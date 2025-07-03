package com.dev.rebook.controllers;

import com.dev.rebook.entities.BookEntity;
import com.dev.rebook.entities.CategoryEntity;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.BookService;

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
    public String getTest(Model model) {
        List<CategoryEntity> CId = this.bookService.getCategoryId();
        model.addAttribute("CId", CId);
        return "test/test";
    }

    // TODO 키워드 기반 컨트롤러 작성

    @RequestMapping(value = "/api/aladin/bestseller", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BookEntity[] getBestSellerBooksFromAladin() {
        ResultTuple<BookEntity[]> result = this.bookService.searchBestSellerBooksFromAlladin();
        if (result.getResult() != CommonResult.SUCCESS) {
            return new BookEntity[0];
        }
        return result.getPayload();
    }

    @GetMapping(value = "/api/aladin/category", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BookEntity[] getCategoryIdBooksFromAladin(@RequestParam("categoryId") String categoryId) {
        ResultTuple<BookEntity[]> result = this.bookService.searchCategoryBooksFromAlladin(categoryId);
        if (result.getResult() != CommonResult.SUCCESS) {
            return new BookEntity[0];
        }
        return result.getPayload();
    }

    @RequestMapping(value = "/api/chat", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getMessage() {
        JSONArray response = new JSONArray();
        for (String msg : messages) {
            response.put(msg);
        }
        return response.toString();
    }
}