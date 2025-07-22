package com.dev.rebook.controllers;

import com.dev.rebook.dtos.ReviewWithProfileDto;
import com.dev.rebook.entities.BookEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.BookService;
import com.dev.rebook.services.ReviewService;
import com.dev.rebook.vos.SearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping(value = "/book")
public class BookController {
    private final BookService bookService;
    private final ReviewService reviewService;

    @Autowired
    public BookController(BookService bookService, ReviewService reviewService) {
        this.bookService = bookService;
        this.reviewService = reviewService;
    }

//    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
//    public String getSearch() {
//        return "book/search";
//    }

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getSearch(@RequestParam(value = "keyword", required = false) String keyword,
                            SearchVo searchVo,
                            Model model) {
        System.out.println("컨트롤러 도착 : " + keyword + "/ SearchVo : getSearchType - " + searchVo.getSearchType() + " getSearchTarget - " + searchVo.getSearchTarget() + " getSort - " + searchVo.getSort());

        ResultTuple<BookEntity[]> result = this.bookService.searchBooksFromAladin(keyword, searchVo);
        System.out.println(result);

        if (result.getResult() != CommonResult.SUCCESS) {
            model.addAttribute("books", null);
        } else {
            model.addAttribute("books", result.getPayload());
        }
        return "book/search";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getIndex(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                           @RequestParam(value = "id", required = false) String id,
                           Model model) {
        BookEntity book = this.bookService.getBookById(id);
        model.addAttribute("book", book);
        ReviewWithProfileDto[] reviews = this.reviewService.getReviewsByBookId(id, signedUser);
        model.addAttribute("reviews", reviews);
        System.out.println(reviews.length);
        return "book/index";
    }
}
