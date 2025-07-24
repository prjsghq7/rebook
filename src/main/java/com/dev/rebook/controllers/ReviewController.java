package com.dev.rebook.controllers;

import com.dev.rebook.dtos.ReviewPageItemDto;
import com.dev.rebook.entities.ReviewEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.results.Result;
import com.dev.rebook.services.ReviewService;
import com.dev.rebook.vos.ReviewPageButtonVo;
import com.dev.rebook.vos.ReviewPageVo;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/review")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getIndex(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                           @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                           ReviewPageButtonVo reviewPageButtonVo,
                           Model model) {
        model.addAttribute("signedUser", signedUser);

        Pair<ReviewPageItemDto[], ReviewPageVo> result;
        result = this.reviewService.getReviewAll(signedUser, page, reviewPageButtonVo);

        model.addAttribute("reviewPageButtonVo", reviewPageButtonVo);
        model.addAttribute("reviews", result.getFirst());
        model.addAttribute("reviewPageVo", result.getSecond());
        return "review/index";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegister(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                               ReviewEntity review) {
        Result result = this.reviewService.insert(signedUser, review);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getRegister(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                              @RequestParam(value = "id", required = false) String id,
                              Model model) {
        model.addAttribute("signedUser", signedUser);

        return "review/register";
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteReview(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                               @RequestParam(value = "id", required = false) int id) {
        Result result = this.reviewService.delete(signedUser, id);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/modify", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchReview(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                              ReviewEntity review) {
        Result result = this.reviewService.modify(signedUser, review);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/modify", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getModify(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                            @RequestParam(value = "id", required = false) int id,
                            Model model) {
        model.addAttribute("signedUser", signedUser);
        ReviewEntity review = this.reviewService.getReviewById(id);
        model.addAttribute("review", review);
        return "review/modify";
    }
}
