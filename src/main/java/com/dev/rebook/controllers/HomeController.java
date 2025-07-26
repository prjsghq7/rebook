package com.dev.rebook.controllers;

import com.dev.rebook.dtos.PopularBookDto;
import com.dev.rebook.entities.BookEntity;
import com.dev.rebook.entities.CategoryEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.BookService;

import com.dev.rebook.services.uesr.UserService;
import com.dev.rebook.utils.Utils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Controller
public class HomeController {


    private final BookService bookService;

    @Autowired
    public HomeController(BookService bookService) {
        this.bookService = bookService;
    }

    private static final List<String> messages = new ArrayList<>();


    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getTest(Model model,
                          @SessionAttribute(value = "signedUser", required = false) UserEntity signedUser) {
        List<CategoryEntity> categories = this.bookService.getCategoryId();
        model.addAttribute("categories", categories);
        if (UserService.isInvalidUser(signedUser)) {
            model.addAttribute("UserCId", categories.get(0).getId());
        } else {
            model.addAttribute("UserCId", signedUser.getCategoryId());
        }
        return "home/home";

    }

    @RequestMapping(value = "/api/aladin/bestseller", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BookEntity[] getBooksBestSeller(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser) {
        boolean isAdult = Utils.isAdult(signedUser);

        ResultTuple<BookEntity[]> result = this.bookService.searchBooksBestSellerAlladin(isAdult);
        if (result.getResult() != CommonResult.SUCCESS) {
            return new BookEntity[0];
        }
        return result.getPayload();
    }

    @RequestMapping(value = "/api/aladin/category", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResultTuple<BookEntity[]> getBooksCategoryId(@RequestParam(value = "categoryId") String categoryId,
                                                        @SessionAttribute(value = "signedUser", required = false) UserEntity signedUser
    ) {
        return this.bookService.searchBooksFromUserCategory(categoryId, signedUser);
    }

    @RequestMapping(value = "/api/aladin/new-top-book", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BookEntity[] getBooksNewTopBooks(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser) {
        boolean isAdult = Utils.isAdult(signedUser);
        ResultTuple<BookEntity[]> result = this.bookService.searchBooksNewAlladin(isAdult);
        if (result.getResult() != CommonResult.SUCCESS) {
            return new BookEntity[0];
        }
        return result.getPayload();
    }

    @RequestMapping(value = "/book/popular-book", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public PopularBookDto[] getUserPopularBooks(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser) {
        boolean isAdult = Utils.isAdult(signedUser);

        return this.bookService.selectPopularUserBooks(isAdult);
    }
}