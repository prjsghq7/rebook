package com.dev.rebook.services;

import com.dev.rebook.entities.BookEntity;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.vos.SearchVo;
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

@Service
public class BookService {

    @Value("${api-aladin-api-key}")
    private String aladinApiKey;

    public ResultTuple<BookEntity[]> searchBooksFromAladin(String keyword, SearchVo searchVo) {
        System.out.println("서비스 도착 : " + keyword);
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
            aladinUrl.append("&MaxResults=10&Cover=Big&start=1&output=xml&Version=20131101");
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

            System.out.println("aladinUrl : " + aladinUrl.toString());


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

    private BookEntity[] parseXmlToBookArray(String xml) {
        List<BookEntity> booksList = new ArrayList<>();

        try {
            // XML 파서 설정
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            // item 태그들 찾기
            NodeList items = document.getElementsByTagName("item");

            // 각 item을 BookEntity로 변환
            for (int i = 0; i < items.getLength(); i++) {
                Element item = (Element) items.item(i);

                BookEntity book = new BookEntity();
                book.setTitle(getElementText(item, "title"));
                book.setAuthor(getElementText(item, "author"));
                book.setPubDate(parsePubDate(getElementText(item, "pubDate")));
                book.setCover(getElementText(item, "cover"));
                book.setLink(getElementText(item, "link"));

                booksList.add(book);
            }

        } catch (Exception e) {
            throw new RuntimeException("XML 파싱 실패: " + e.getMessage());
        }

        // List를 배열로 변환하여 반환
        return booksList.toArray(new BookEntity[0]);
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
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
}
