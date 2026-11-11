package com.dev.rebook.services;

import com.dev.rebook.dtos.PopularBookDto;
import com.dev.rebook.entities.BookEntity;
import com.dev.rebook.entities.CategoryEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.mappers.BookMapper;
import com.dev.rebook.mappers.CategoryMapper;
import com.dev.rebook.mappers.ReviewMapper;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.utils.Utils;
import com.dev.rebook.vos.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BookService {
    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;
    private final ReviewMapper reviewMapper;

    // 카카오(Daum) 책 검색 REST 키 = 카카오 로그인 앱의 REST API 키와 동일 (Render 환경변수 KAKAO_CLIENT_ID)
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoRestApiKey;

    @Autowired
    public BookService(BookMapper bookMapper, CategoryMapper categoryMapper, ReviewMapper reviewMapper) {
        this.bookMapper = bookMapper;
        this.categoryMapper = categoryMapper;
        this.reviewMapper = reviewMapper;
    }

    /*
     * 알라딘 OpenAPI 종료(2026-10-30), 네이버 책 검색 API 종료(2026-07-31)로 카카오(Daum) 책 검색 API 사용.
     * 흐름은 그대로: 검색 → books에 없으면 insert → DB에서 꺼내 반환. DB 구조 변경 없음.
     * 쿼터: 책 검색 일 30,000건.
     */
    private static final String KAKAO_BOOK_URL = "https://dapi.kakao.com/v3/search/book";
    private static final int SEARCH_SIZE = 12;
    private static final String NO_COVER = "/book/assets/images/no-cover.svg";
    private static final int VARCHAR_MAX = 255; // books의 title / link / author / cover / publisher 길이

    // 카테고리 이름 그대로 검색하면 0건인 경우가 많아("가정 요리 뷰티" 등) 카테고리별 대표 키워드로 검색 (categories.id 기준)
    private static final Map<String, String> CATEGORY_KEYWORDS = Map.ofEntries(
            Map.entry("8416", "심리학"),
            Map.entry("48828", "자기계발"),
            Map.entry("656", "인문학"),
            Map.entry("97202", "로맨스 소설"),
            Map.entry("6545", "공포 소설"),
            Map.entry("6684", "판타지 소설"),
            Map.entry("177", "창업"),
            Map.entry("51312", "천문학"),
            Map.entry("53533", "식물 키우기"),
            Map.entry("55890", "취미"),
            Map.entry("53486", "인테리어"),
            Map.entry("53534", "반려동물"),
            Map.entry("51660", "사주"),
            Map.entry("51667", "철학"),
            Map.entry("51767", "건강"),
            Map.entry("1230", "요리"),
            Map.entry("2225", "재테크")
    );

    public ResultTuple<BookEntity[]> searchBooks(boolean isAdult, String keyword, SearchVo searchVo) {
        if (keyword == null || keyword.isBlank()) {
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }

        try {
            String searchType = searchVo == null || searchVo.getSearchType() == null ? "-1" : searchVo.getSearchType();
            String sort = searchVo == null || searchVo.getSort() == null ? "-1" : searchVo.getSort();

            // 검색어 종류: 제목 / 저자(person) / 출판사. 제목+저자·미선택은 전체 검색
            String target = switch (searchType.toLowerCase()) {
                case "title" -> "title";
                case "author" -> "person";
                case "publisher" -> "publisher";
                default -> null;
            };

            UriComponentsBuilder uri = UriComponentsBuilder
                    .fromHttpUrl(KAKAO_BOOK_URL)
                    .queryParam("query", keyword.trim())
                    .queryParam("size", SEARCH_SIZE)
                    // 카카오는 정확도(accuracy) / 발간일(latest)만 지원. 제목·판매량 정렬은 정확도로 대체
                    .queryParam("sort", "publishTime".equals(sort) ? "latest" : "accuracy");
            if (target != null) {
                uri.queryParam("target", target);
            }
            // searchTarget(도서/외국도서/전자책)은 카카오에 해당 옵션이 없어 무시

            BookEntity[] books = parseKakaoJsonToBookArray(requestKakao(uri), isAdult);
            return ResultTuple.<BookEntity[]>builder()
                    .payload(books)
                    .result(CommonResult.SUCCESS)
                    .build();
        } catch (Exception e) {
            log.warn("카카오 책 검색 실패: {}", e.getMessage());
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }
    }

    private String requestKakao(UriComponentsBuilder uri) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 외부 API가 느려도 페이지가 무한 대기하지 않도록
        factory.setReadTimeout(5000);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
        URI target = uri.encode(StandardCharsets.UTF_8).build().toUri();
        return new RestTemplate(factory)
                .exchange(target, HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getBody();
    }

    private BookEntity[] parseKakaoJsonToBookArray(String json, boolean isAdult) {
        List<BookEntity> booksList = new ArrayList<>();
        JSONArray documents = new JSONObject(json).getJSONArray("documents");

        for (int i = 0; i < documents.length(); i++) {
            JSONObject doc = documents.getJSONObject(i);
            String isbn = pickIsbn(doc.optString("isbn", null));
            if (isbn == null) {
                continue; // ISBN 없는 항목은 id를 만들 수 없어 건너뜀
            }

            if (this.bookMapper.selectCountById(isbn) <= 0) {
                try {
                    this.bookMapper.insert(toBookEntity(isbn, doc));
                } catch (Exception e) {
                    // 한 권 저장 실패로 검색 결과 전체가 비지 않도록 해당 책만 건너뜀
                    log.warn("도서 저장 실패 ({}): {}", isbn, e.getMessage());
                    continue;
                }
            }
            booksList.add(getAdultVerification(isbn, isAdult));
        }
        return booksList.toArray(new BookEntity[0]);
    }

    private BookEntity toBookEntity(String isbn, JSONObject doc) {
        BookEntity book = new BookEntity();
        book.setId(isbn);
        book.setTitle(limit(doc.optString("title", "").trim()));

        List<String> authors = new ArrayList<>();
        JSONArray authorArray = doc.optJSONArray("authors");
        if (authorArray != null) {
            for (int j = 0; j < authorArray.length(); j++) {
                String name = authorArray.optString(j, "").trim();
                if (!name.isEmpty()) {
                    authors.add(name);
                }
            }
        }
        book.setAuthor(authors.isEmpty() ? null : limit(String.join(", ", authors)));

        // datetime: "2009-01-20T00:00:00.000+09:00"
        String datetime = doc.optString("datetime", "");
        book.setPubDate(datetime.length() >= 10 ? parsePubDate(datetime.substring(0, 10)) : null);

        // 썸네일은 120x174라 작음 → 같은 썸네일 서버에서 너비 300 버전 사용. 없으면 기본 표지
        String thumbnail = doc.optString("thumbnail", "").trim();
        String cover = thumbnail.isEmpty() ? NO_COVER : thumbnail.replace("/thumb/R120x174", "/thumb/R300x0");
        book.setCover(cover.length() > VARCHAR_MAX ? NO_COVER : cover);

        // 상세 URL은 검색어(&q=...)가 붙어 255자를 넘을 수 있음 → bookId까지만 저장
        String url = doc.optString("url", "");
        int q = url.indexOf("&q=");
        book.setLink(limit(q >= 0 ? url.substring(0, q) : url));

        book.setDescription(doc.optString("contents", null));
        int salePrice = doc.optInt("sale_price", -1); // 판매가 없으면 -1 (price_sales는 음수 불가)
        book.setPriceSales(salePrice >= 0 ? salePrice : null);
        book.setMallType("BOOK");
        book.setPublisher(limit(doc.optString("publisher", "").trim()));
        book.setAdult(false); // 카카오 응답에 성인 여부 없음
        book.setView(0);
        return book;
    }

    // isbn: "10자리 13자리" / "13자리" / " 13자리" → 13자리 우선, 없으면 10자리
    private String pickIsbn(String raw) {
        if (raw == null) {
            return null;
        }
        String fallback = null;
        for (String token : raw.trim().split("\\s+")) {
            String digits = token.replaceAll("[^0-9Xx]", "");
            if (digits.length() == 13) {
                return digits;
            }
            if (digits.length() == 10 && fallback == null) {
                fallback = digits;
            }
        }
        return fallback;
    }

    private String limit(String value) {
        return value.length() > VARCHAR_MAX ? value.substring(0, VARCHAR_MAX) : value;
    }

    private BookEntity[] maskAdult(BookEntity[] books, boolean isAdult) {
        if (!isAdult) {
            for (BookEntity book : books) {
                if (book.isAdult()) {
                    book.setCover("/book/assets/images/adult-limit.png");
                }
            }
        }
        return books;
    }

    // ---- 아래는 알라딘 XML 파서 (API 종료로 현재 미사용, 기존 데이터 형식 참고용으로 보존) ----
    private BookEntity[] parseXmlToBookArray(String xml, boolean isAdult) {
        List<BookEntity> booksList = new ArrayList<>();

        try {
            // XML 파서 설정
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            // item 태그들 찾기
            NodeList items = document.getElementsByTagName("item");

            int insertResult = 0; // 신규 추가된 DB

            // 각 item을 BookEntity로 변환
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);
                BookEntity book = new BookEntity();

                book.setId(getValidIsbn(item)); // isbn13, isbn 검사 후 id 세팅
                book.setTitle(getElementText(item, "title", false));
                book.setAuthor(getElementText(item, "author", true));
                book.setPubDate(parsePubDate(getElementText(item, "pubDate", true)));
                book.setCover(getElementText(item, "cover", true));
                book.setLink(getElementText(item, "link", false));
                book.setDescription(getElementText(item, "description", true));
                book.setPriceSales(getElementInt(item, "priceSales", true));
                book.setMallType(getElementText(item, "mallType", false));
                book.setPublisher(getElementText(item, "publisher", false));
                book.setAdult(parseBoolean(getElementText(item, "adult", false)));
                book.setView(0);

                // DB에 있으면 DB에서 꺼내 저장 없으면 신규로 Insert하기
                if (this.bookMapper.selectCountById(book.getId()) <= 0) {
                    insertResult += this.bookMapper.insert(book);
                }

//                booksList.add(this.bookMapper.selectById(book.getId()));
                booksList.add(getAdultVerification(book.getId(), isAdult));

            }
        } catch (Exception e) {
            throw new RuntimeException("XML 파싱 실패: " + e.getMessage());
        }

        // List를 배열로 변환하여 반환
        return booksList.toArray(new BookEntity[0]);
    }

    private BookEntity getAdultVerification(String bookId, boolean isAdult) {
        BookEntity result = this.bookMapper.selectById(bookId);
        if (result.isAdult()) {
            if (!isAdult) {
                result.setCover("/book/assets/images/adult-limit.png");
            }
        }
        return result;
    }

    private String getElementText(Element parent, String tagName, boolean nullable) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            String value = nodeList.item(0).getTextContent();
            if (value == null || value.trim().isEmpty()) {
                return nullable ? null : "";
            }
            return value;
        }
        return nullable ? null : "";
    }

    private Integer getElementInt(Element parent, String tagName, boolean nullable) {
        String value = getElementText(parent, tagName, nullable);
        if (value == null || value.trim().isEmpty()) {
            return nullable ? null : 0;
        }
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return nullable ? null : 0;
        }
    }

    private LocalDate parsePubDate(String pubDateStr) {
        if (pubDateStr == null || pubDateStr.trim().isEmpty()) {
            return null;
        }

        try {
            // 알라딘 API의 날짜 형식: "2024-01-01" 또는 "2024-01-01 00:00:00"
            String cleanDate = pubDateStr.split(" ")[0]; // 시간 부분 제거
            return LocalDate.parse(cleanDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            // 파싱 실패 시 null 반환
            return null;
        }
    }

    private boolean parseBoolean(String boolStr) {
        return "true".equalsIgnoreCase(boolStr) || "1".equals(boolStr);
    }

    // isbn13, isbn 중 유효한 값을 id로 반환, 없으면 예외
    private String getValidIsbn(Element item) {
        String isbn13 = getElementText(item, "isbn13", false);
        String isbn = getElementText(item, "isbn", false);
        if (isValidIsbn(isbn13)) {
            return isbn13.trim();
        }
        if (isValidIsbn(isbn)) {
            return isbn.trim();
        }
        throw new IllegalArgumentException("ISBN 정보가 없습니다.");
    }

    // null, 빈 문자열, '0'이 아닌지 검사
    private boolean isValidIsbn(String isbn) {
        return isbn != null && !isbn.trim().isEmpty() && !"0".equals(isbn.trim());
    }

    // 베스트셀러: 알라딘 대신 우리 DB의 조회수 + 리뷰 수 기준
    public ResultTuple<BookEntity[]> searchBooksBestSeller(boolean isAdult) {
        try {
            return ResultTuple.<BookEntity[]>builder()
                    .payload(maskAdult(this.bookMapper.selectMostPopular(SEARCH_SIZE), isAdult))
                    .result(CommonResult.SUCCESS)
                    .build();
        } catch (Exception e) {
            log.warn("베스트셀러 조회 실패: {}", e.getMessage());
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }
    }

    // 신간: 알라딘 대신 우리 DB의 출간일 최신순
    public ResultTuple<BookEntity[]> searchBooksNew(boolean isAdult) {
        try {
            return ResultTuple.<BookEntity[]>builder()
                    .payload(maskAdult(this.bookMapper.selectNewest(4), isAdult))
                    .result(CommonResult.SUCCESS)
                    .build();
        } catch (Exception e) {
            log.warn("신간 조회 실패: {}", e.getMessage());
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }
    }

    // 카테고리: 카카오에 카테고리 필터가 없어 카테고리별 대표 키워드로 검색 (없으면 카테고리 이름의 첫 단어)
    public ResultTuple<BookEntity[]> searchBooksFromUserCategory(String categoryId, UserEntity signedUser) {
        String keyword = CATEGORY_KEYWORDS.get(categoryId);
        if (keyword == null) {
            keyword = this.categoryMapper.selectAll().stream()
                    .filter(c -> c.getId().equals(categoryId))
                    .map(c -> c.getDisplayText().split("/")[0])
                    .findFirst()
                    .orElse(null);
        }
        if (keyword == null) {
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }
        SearchVo searchVo = new SearchVo("-1", "-1", "-1");
        return searchBooks(Utils.isAdult(signedUser), keyword, searchVo);
    }

    public PopularBookDto[] selectPopularUserBooks(boolean isAdult) {
        PopularBookDto[] books = this.reviewMapper.selectPopularUserBooks();
        for (PopularBookDto book : books) {
            if (book.isAdult()) {
                if(!isAdult) {
                    book.setCover("/book/assets/images/adult-limit.png");
                }
            }
        }

        return books;
    }

    // 카테고리 thymeleaf
    public List<CategoryEntity> getCategoryId() {
        return categoryMapper.selectAll();
    }

    public BookEntity getBookById(String id) {
        return this.bookMapper.selectById(id);
    }

    public void incrementView(String id) {
        this.bookMapper.increaseView(id);
    }
}