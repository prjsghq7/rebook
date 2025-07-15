package com.dev.rebook.controllers;

import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.services.ReviewService;
import com.dev.rebook.services.uesr.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping(value = "/review")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getInfo(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                          @RequestParam(value = "id", required = false) String id,
                          Model model) {
        model.addAttribute("signedUser", signedUser);

        return "review/register";
    }
}
