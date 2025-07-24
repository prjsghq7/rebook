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
import com.dev.rebook.services.uesr.UserService;
import com.dev.rebook.vos.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
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

    @Value("${api-aladin-api-key}")
    private String aladinApiKey;

    @Autowired
    public BookService(BookMapper bookMapper, CategoryMapper categoryMapper, ReviewMapper reviewMapper) {
        this.bookMapper = bookMapper;
        this.categoryMapper = categoryMapper;
        this.reviewMapper = reviewMapper;
    }

    public ResultTuple<BookEntity[]> searchBooksFromAladin(String keyword, SearchVo searchVo) {
        if (keyword == null || keyword.isEmpty()) {
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }

        try {
            // 키워드 URL 인코딩
//            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            // 알라딘 API URL 생성
            /*String aladinUrl = String.format(
                    "http://www.aladin.co.kr/ttb/api/ItemSearch.aspx?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=10&Cover=Big&start=1&SearchTarget=Book&output=xml&Version=20131101",
                    aladinApiKey, keyword);*/

            StringBuilder aladinUrl = new StringBuilder();
            aladinUrl.append("http://www.aladin.co.kr/ttb/api/ItemSearch.aspx?ttbkey=")
                    .append(aladinApiKey); // API 키
            aladinUrl.append("&MaxResults=12&Cover=Big&start=1&output=xml&Version=20131101");
            aladinUrl.append("&Query=")
                    .append(keyword); // 검색어 키워드

            /*
             * 아래 QueryType, SearchTarget, Sort 없어도 기본으로 값 들어감
             * QueryType : 제목+저자
             * SearchTarget : 도서
             * Sort : 관련도
             * */
            if (!searchVo.getSearchType().equals("-1")) {
                aladinUrl.append("&QueryType=")
                        .append(searchVo.getSearchType()); // 검색어 종류 ( 제목 + 저자, 저자, 제목 등 )
            }
            if (!searchVo.getSearchTarget().equals("-1")) {
                aladinUrl.append("&SearchTarget=")
                        .append(searchVo.getSearchTarget()); // 검색 대상 Mall ( 도서, 외국도서, eBook 등 )
            }
            if (!searchVo.getSort().equals("-1")) {
                aladinUrl.append("&Sort=")
                        .append(searchVo.getSort()); // 정렬순서 ( 출간일, 판매량, 제목 등 )
            }


            // RestTemplate으로 API 호출
            RestTemplate restTemplate = new RestTemplate();
            String xmlResponse = restTemplate.getForObject(aladinUrl.toString(), String.class);
            // XML 응답을 BookEntity 배열로 파싱
            BookEntity[] books = parseXmlToBookArray(xmlResponse);
            return ResultTuple.<BookEntity[]>builder()
                    .payload(books)
                    .result(CommonResult.SUCCESS)
                    .build();
        } catch (Exception e) {
            System.out.println("bookService : searchBooksFromAladin 예외처리");
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }
    }

    private BookEntity[] parseXmlToBookArray(String xml) {
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

                // DB에 있으면 DB에서 꺼내 저장 없으면 신규로 Insert하기
                if (this.bookMapper.selectCountById(book.getId()) <= 0) {
                    insertResult += this.bookMapper.insert(book);
                }
                booksList.add(this.bookMapper.selectById(book.getId()));
            }
            if (insertResult > 0) {
                System.out.println("DB에 신규 추가된 책 개수 : " + insertResult);
            }
        } catch (Exception e) {
            throw new RuntimeException("XML 파싱 실패: " + e.getMessage());
        }

        // List를 배열로 변환하여 반환
        return booksList.toArray(new BookEntity[0]);
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
            System.out.println("ISBN 10자리 등록됨");
            return isbn.trim();
        }
        throw new IllegalArgumentException("ISBN 정보가 없습니다.");
    }

    // null, 빈 문자열, '0'이 아닌지 검사
    private boolean isValidIsbn(String isbn) {
        return isbn != null && !isbn.trim().isEmpty() && !"0".equals(isbn.trim());
    }

    public ResultTuple<BookEntity[]> searchBooksBestSellerAlladin() {
        try {
            StringBuilder aladinUrl = new StringBuilder();
            aladinUrl.append("http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=")
                    .append(aladinApiKey); // API 키
            aladinUrl.append("&QueryType=BestSeller&Cover=Big&MaxResults=12&start=1&SearchTarget=Book&output=xml&Version=20131101");


            // RestTemplate으로 API 호출
            RestTemplate restTemplate = new RestTemplate();
            String xmlResponse = restTemplate.getForObject(aladinUrl.toString(), String.class);

            // XML 응답을 BookEntity 배열로 파싱
            BookEntity[] books = parseXmlToBookArray(xmlResponse);
            System.out.println(books[0].getTitle());
            return ResultTuple.<BookEntity[]>builder()
                    .payload(books)
                    .result(CommonResult.SUCCESS)
                    .build();
        } catch (Exception e) {
            System.out.println("bookService : searchBooksFromAladin 예외처리");
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }
    }
    public ResultTuple<BookEntity[]> searchBooksNewAlladin() {
        try {
            StringBuilder aladinUrl = new StringBuilder();
            aladinUrl.append("http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=")
                    .append(aladinApiKey); // API 키
            aladinUrl.append("&QueryType=ItemNewAll&Cover=Big&MaxResults=4&start=1&SearchTarget=Book&output=xml&Version=20131101");


            // RestTemplate으로 API 호출
            RestTemplate restTemplate = new RestTemplate();
            String xmlResponse = restTemplate.getForObject(aladinUrl.toString(), String.class);

            // XML 응답을 BookEntity 배열로 파싱
            BookEntity[] books = parseXmlToBookArray(xmlResponse);
            System.out.println(books[0].getTitle());
            return ResultTuple.<BookEntity[]>builder()
                    .payload(books)
                    .result(CommonResult.SUCCESS)
                    .build();
        } catch (Exception e) {
            System.out.println("bookService : searchBooksFromAladin 예외처리");
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }
    }

    public ResultTuple<BookEntity[]> searchBooksFromUserKeyword(String categoryId ,UserEntity signedUser) {
        try {

            StringBuilder aladinUrl = new StringBuilder();
            aladinUrl.append("http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=")
                    .append(aladinApiKey);
            aladinUrl.append("&QueryType=BestSeller");
            aladinUrl.append("&CategoryId=").append(categoryId);
            aladinUrl.append("&MaxResults=12&Cover=Big&start=1&output=xml&Version=20131101");


            // RestTemplate으로 API 호출
            RestTemplate restTemplate = new RestTemplate();
            String xmlResponse = restTemplate.getForObject(aladinUrl.toString(), String.class);

            // XML 응답을 BookEntity 배열로 파싱
            BookEntity[] books = parseXmlToBookArray(xmlResponse);
            System.out.println(books);
            System.out.println(books[0].getTitle());
            return ResultTuple.<BookEntity[]>builder()
                    .payload(books)
                    .result(CommonResult.SUCCESS)
                    .build();
        } catch (Exception e) {
            System.out.println("bookService : searchBooksFromAladin 예외처리");
            return ResultTuple.<BookEntity[]>builder().result(CommonResult.FAILURE).build();
        }
    }

    public PopularBookDto[] selectPopularUserBooks() {
        return this.reviewMapper.selectPopularUserBooks();
    }

    // 카테고리 thymeleaf
    public List<CategoryEntity> getCategoryId() {
        return categoryMapper.selectAll();
    }

    public BookEntity getBookById(String id) {
        return this.bookMapper.selectById(id);
    }
}