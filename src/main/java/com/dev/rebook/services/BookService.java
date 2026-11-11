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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

@Slf4j
@Service
public class BookService {
    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;
    private final ReviewMapper reviewMapper;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    @Autowired
    public BookService(BookMapper bookMapper, CategoryMapper categoryMapper, ReviewMapper reviewMapper) {
        this.bookMapper = bookMapper;
        this.categoryMapper = categoryMapper;
        this.reviewMapper = reviewMapper;
    }

    /*
     * 알라딘 OpenAPI 종료(2026-10-30)로 네이버 책 검색 API로 교체.
     * 흐름은 그대로: 검색 → books에 없으면 insert → DB에서 꺼내 반환. DB 구조 변경 없음.
     * 키는 Re:Book 네이버 로그인 앱의 client-id / secret을 같이 사용 (네이버 앱에 '검색' API 추가 필요).
     */
    private static final String NAVER_BOOK_URL = "https://openapi.naver.com/v1/search/book.xml";
    private static final String NAVER_BOOK_ADV_URL = "https://openapi.naver.com/v1/search/book_adv.xml";
    private static final int SEARCH_SIZE = 12;

    public ResultTuple<BookEntity[]> searchBooks(boolean isAdult, String keyword, SearchVo searchVo) {
        if (keyword == null || keyword.isBlank()) {
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }

        try {
            String searchType = searchVo == null || searchVo.getSearchType() == null ? "-1" : searchVo.getSearchType();
            String sort = searchVo == null || searchVo.getSort() == null ? "-1" : searchVo.getSort();

            // 제목 / 저자 / 출판사 검색은 상세검색 API, 그 외(제목+저자, 미선택)는 기본 검색 API
            String advParam = switch (searchType.toLowerCase()) {
                case "title" -> "d_titl";
                case "author" -> "d_auth";
                case "publisher" -> "d_publ";
                default -> null;
            };

            UriComponentsBuilder uri = UriComponentsBuilder
                    .fromHttpUrl(advParam == null ? NAVER_BOOK_URL : NAVER_BOOK_ADV_URL)
                    .queryParam(advParam == null ? "query" : advParam, keyword.trim())
                    .queryParam("display", SEARCH_SIZE)
                    .queryParam("start", 1)
                    // 네이버는 관련도(sim) / 출간일(date)만 지원. 제목·판매량 정렬은 관련도로 대체
                    .queryParam("sort", "publishTime".equals(sort) ? "date" : "sim");
            // searchTarget(도서/외국도서/전자책)은 네이버에 해당 옵션이 없어 무시

            BookEntity[] books = parseNaverXmlToBookArray(requestNaver(uri), isAdult);
            return ResultTuple.<BookEntity[]>builder()
                    .payload(books)
                    .result(CommonResult.SUCCESS)
                    .build();
        } catch (Exception e) {
            log.warn("네이버 책 검색 실패: {}", e.getMessage());
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }
    }

    private String requestNaver(UriComponentsBuilder uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);
        URI target = uri.encode(StandardCharsets.UTF_8).build().toUri();
        return new RestTemplate()
                .exchange(target, HttpMethod.GET, new HttpEntity<>(headers), String.class)
                .getBody();
    }

    private BookEntity[] parseNaverXmlToBookArray(String xml, boolean isAdult) throws Exception {
        List<BookEntity> booksList = new ArrayList<>();
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
        NodeList items = document.getElementsByTagName("item");

        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            String isbn = pickIsbn(getElementText(item, "isbn", true));
            if (isbn == null) {
                continue; // ISBN 없는 항목은 id를 만들 수 없어 건너뜀
            }

            if (this.bookMapper.selectCountById(isbn) <= 0) {
                BookEntity book = new BookEntity();
                book.setId(isbn);
                book.setTitle(stripTags(getElementText(item, "title", false)));
                String author = stripTags(getElementText(item, "author", true));
                book.setAuthor(author == null ? null : author.replace("^", ", ")); // 네이버는 저자 구분자가 ^
                book.setPubDate(parseNaverPubDate(getElementText(item, "pubdate", true)));
                book.setCover(getElementText(item, "image", true));
                book.setLink(getElementText(item, "link", false));
                book.setDescription(stripTags(getElementText(item, "description", true)));
                book.setPriceSales(getElementInt(item, "discount", true));
                book.setMallType("BOOK");
                book.setPublisher(stripTags(getElementText(item, "publisher", false)));
                book.setAdult(false); // 네이버 응답에 성인 여부 없음
                book.setView(0);
                this.bookMapper.insert(book);
            }
            booksList.add(getAdultVerification(isbn, isAdult));
        }
        return booksList.toArray(new BookEntity[0]);
    }

    // 네이버 isbn: "13자리" 또는 "10자리 13자리" → 13자리 우선
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

    private LocalDate parseNaverPubDate(String raw) {
        if (raw == null || !raw.trim().matches("\\d{8}")) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim(), DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String stripTags(String value) {
        return value == null ? null : value.replaceAll("<[^>]+>", "").trim();
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

    // 카테고리: 네이버에 카테고리 필터가 없어 카테고리 이름("경제/투자" → "경제 투자")으로 검색
    public ResultTuple<BookEntity[]> searchBooksFromUserCategory(String categoryId, UserEntity signedUser) {
        String keyword = this.categoryMapper.selectAll().stream()
                .filter(c -> c.getId().equals(categoryId))
                .map(c -> c.getDisplayText().replace("/", " "))
                .findFirst()
                .orElse(null);
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