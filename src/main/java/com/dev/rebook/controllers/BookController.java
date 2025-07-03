package com.dev.rebook.controllers;

import com.dev.rebook.entities.BookEntity;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.BookService;
import com.dev.rebook.vos.SearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/book")
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getSearch() {
        return "book/search";
    }

    @RequestMapping(value = "/search-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BookEntity[] getSearchList(@RequestParam(value = "keyword", required = false) String keyword,
                                      SearchVo searchVo) {
        System.out.println("컨트롤러 도착 : " + keyword);
        System.out.println("SearchVo : getSearchType - " + searchVo.getSearchType() + " getSearchTarget - " + searchVo.getSearchTarget() + " getSort - " + searchVo.getSort());
        if (keyword == null || keyword.isEmpty()) {
            return new BookEntity[0];
        }

        ResultTuple<BookEntity[]> result = this.bookService.searchBooksFromAladin(keyword, searchVo);
        System.out.println(result);

        if (result.getResult() != CommonResult.SUCCESS) {
            return new BookEntity[0];
        }
        return result.getPayload();
    }
}
