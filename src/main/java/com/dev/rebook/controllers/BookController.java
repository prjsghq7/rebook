package com.dev.rebook.controllers;

import com.dev.rebook.dtos.ReviewPageItemDto;
import com.dev.rebook.dtos.ReviewWithProfileDto;
import com.dev.rebook.entities.BookEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.BookService;
import com.dev.rebook.services.ReviewService;
import com.dev.rebook.services.uesr.UserService;
import com.dev.rebook.utils.Utils;
import com.dev.rebook.vos.ReviewPageVo;
import com.dev.rebook.vos.SearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
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

    @RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getSearch(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            SearchVo searchVo,
                            Model model) {
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("keyword", keyword);
        }

        if (searchVo.getSearchType() != null &&
                searchVo.getSearchTarget() != null &&
                searchVo.getSort() != null) {
            model.addAttribute("searchVo", searchVo);
        }

        boolean isAdult = Utils.isAdult(signedUser);

        ResultTuple<BookEntity[]> result = this.bookService.searchBooksFromAladin(isAdult, keyword, searchVo);

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
                           @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                           Model model) {
        this.bookService.incrementView(id);
        BookEntity book = this.bookService.getBookById(id);

        boolean isAdult = Utils.isAdult(signedUser);
        if (!book.isAdult() || isAdult) {
            model.addAttribute("book", book);
            Pair<ReviewWithProfileDto[], ReviewPageVo> reviewPair = this.reviewService.getReviewsByBookId(id, signedUser, page);
            model.addAttribute("reviews", reviewPair.getFirst());
            model.addAttribute("reviewPageVo", reviewPair.getSecond());
        }
        model.addAttribute("verification", !book.isAdult() || isAdult);
        model.addAttribute("noLoginState", UserService.isInvalidUser(signedUser));
        return "book/index";
    }
}
