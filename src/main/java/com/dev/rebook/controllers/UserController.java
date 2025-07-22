package com.dev.rebook.controllers;

import com.dev.rebook.dtos.RecentReviewDto;
import com.dev.rebook.dtos.ReviewSummaryDto;
import com.dev.rebook.entities.CategoryEntity;
import com.dev.rebook.entities.ContactMvnoEntity;
import com.dev.rebook.entities.EmailTokenEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.ReviewService;
import com.dev.rebook.services.uesr.EmailTokenService;
import com.dev.rebook.services.uesr.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    private final UserService userService;
    private final EmailTokenService emailTokenService;
    private final ReviewService reviewService;

    @Autowired
    public UserController(UserService userService, EmailTokenService emailTokenService, ReviewService reviewService) {
        this.userService = userService;
        this.emailTokenService = emailTokenService;
        this.reviewService = reviewService;
    }

    @RequestMapping(value = "/modify", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchModify(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, UserEntity user) {
        Result result = userService.modify(user, signedUser);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/modify", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getModify(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                          Model model) {
        if (UserService.isInvalidUser(signedUser)) {
            return "redirect:/user/login";
        }
        model.addAttribute("user", signedUser);
        ContactMvnoEntity[] contactMvnos = userService.getContactMvnos();
        model.addAttribute("contactMvnos", contactMvnos);
        List<CategoryEntity> categories = userService.getCategories();
        model.addAttribute("categories", categories);
        return "user/modify";
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getInfo(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                          Model model) {
        if (UserService.isInvalidUser(signedUser)) {
            return "redirect:/user/login";
        }
        model.addAttribute("signedUser", signedUser);
        model.addAttribute("recentReviews", this.reviewService.getRecentReviews(signedUser.getId()));
        System.out.println(this.reviewService.getRecentReviews(signedUser.getId()) == null);
        RecentReviewDto[] reviews = this.reviewService.getRecentReviews(signedUser.getId());
        System.out.println(reviews.length);
        model.addAttribute("reviewSummary", this.reviewService.getReviewSummary(signedUser.getId()));
        return "user/info";
    }

    @RequestMapping(value = "/recover-password-email", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRecoverPasswordEmail(@RequestParam(value = "email") String email,
                                           HttpServletRequest request) throws MessagingException {
        String userAgent = request.getHeader("User-Agent");
        ResultTuple<EmailTokenEntity> result = this.userService.sendRecoverPasswordEmail(email, userAgent);
        JSONObject response = new JSONObject();
        response.put("result", result.getResult().toStringLower());
        if (result.getResult() == CommonResult.SUCCESS) {
            response.put("salt", result.getPayload().getSalt());
        }
        return response.toString();
    }

    @RequestMapping(value = "/recover-password-email", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchRecoverPasswordEmail(EmailTokenEntity emailToken,
                                            HttpServletRequest request) {
        emailToken.setUserAgent(request.getHeader("User-Agent"));
        Result result = this.emailTokenService.verityEmailToken(emailToken);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/recover-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRecoverPassword(EmailTokenEntity emailToken,
                                      HttpServletRequest request,
                                      @RequestParam(value = "password", required = false) String password) {
        emailToken.setUserAgent(request.getHeader("User-Agent"));
        Result result = this.userService.recoverPassword(emailToken, password);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/recover-email", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRecoverEmail(UserEntity user) {
        Result result = this.userService.recoverEmail(user);
        JSONObject response = new JSONObject();
        response.put("result", result.toString().toLowerCase());
        if (result == CommonResult.SUCCESS
                || result == CommonResult.FAILURE_SUSPENDED) {
            response.put("email", user.getEmail());
        }
        return response.toString();
    }

    @RequestMapping(value = "/recover", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getRecover(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                             Model model) {
        if (!UserService.isInvalidUser(signedUser)) {
            return "redirect:/";
        }
        ContactMvnoEntity[] contactMvnos = userService.getContactMvnos();
        model.addAttribute("contactMvnos", contactMvnos);
        return "user/recover";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegister(@RequestParam(value = "registerType", required = false, defaultValue = "local") String registerType,
                               EmailTokenEntity emailToken,
                               UserEntity user,
                               HttpServletRequest request,
                               HttpSession session) {
        emailToken.setUserAgent(request.getHeader("User-Agent"));
        OAuth2User oauthUser = null;
        if (registerType.equals("oauth")) {
            oauthUser = (OAuth2User) session.getAttribute("oauthUser");
        }
        Result result = this.userService.register(emailToken, user, registerType, oauthUser);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getRegister(@RequestParam(value = "registerType", required = false, defaultValue = "local") String registerType,
                              HttpSession session, Model model) {
        UserEntity user = (UserEntity) session.getAttribute("signedUser");
        if (!UserService.isInvalidUser(user)) {
            return "redirect:/";
        }
        if (registerType != null && registerType.equals("local")) {
            session.removeAttribute("oauthUser");
        }
        OAuth2User oauthUser = (OAuth2User) session.getAttribute("oauthUser");
        if (oauthUser != null) {
            model.addAttribute("email", oauthUser.getAttribute("email"));
            model.addAttribute("provider", oauthUser.getAttribute("provider"));
            model.addAttribute("name", oauthUser.getAttribute("name"));
        }

        ContactMvnoEntity[] contactMvnos = userService.getContactMvnos();
        model.addAttribute("contactMvnos", contactMvnos);
        List<CategoryEntity> categories = userService.getCategories();
        model.addAttribute("categories", categories);
        return "user/register";
    }

    @RequestMapping(value = "/register-email", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchRegisterEmail(EmailTokenEntity emailToken,
                                     HttpServletRequest request) {
        emailToken.setUserAgent(request.getHeader("User-Agent"));
        Result result = this.emailTokenService.verityEmailToken(emailToken);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }


    @RequestMapping(value = "/register-email", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegisterEmail(@RequestParam(value = "email") String email,
                                    HttpServletRequest request) throws MessagingException {
        String userAgent = request.getHeader("User-Agent");
        ResultTuple<EmailTokenEntity> result = this.userService.sendRegisterEmail(email, userAgent);
        JSONObject response = new JSONObject();
        response.put("result", result.getResult().toStringLower());
        if (result.getResult() == CommonResult.SUCCESS) {
            response.put("salt", result.getPayload().getSalt());
        }
        return response.toString();
    }

    @RequestMapping(value = "/nickname-check", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postNicknameCheck(@RequestParam(value = "nickname") String nickname) {
        Result result = this.userService.checkNickname(nickname);
        JSONObject response = new JSONObject();
        response.put("result", result.toString().toLowerCase());
        return response.toString();
    }

    @RequestMapping(value = "/contact-check", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postContactCheck(@RequestParam(value = "contactFirst") String contactFirst,
                                   @RequestParam(value = "contactSecond") String contactSecond,
                                   @RequestParam(value = "contactThird") String contactThird) {
        Result result = this.userService.checkContact(contactFirst, contactSecond, contactThird);
        JSONObject response = new JSONObject();
        response.put("result", result.toString().toLowerCase());
        return response.toString();
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_HTML_VALUE)
    public String getLogout(HttpSession session) {
        session.setAttribute("signedUser", null);
        return "redirect:/user/login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postLogin(@RequestParam(value = "email") String email,
                            @RequestParam(value = "password") String password,
                            HttpSession session,
                            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        ResultTuple<UserEntity> result = userService.login(email, password, userAgent);
        if (result.getResult() == CommonResult.SUCCESS) {
            session.setAttribute("signedUser", result.getPayload());
        }
        JSONObject response = new JSONObject();
        response.put("result", result.getResult().toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getLogin(@SessionAttribute(value = "signedUser", required = false) UserEntity user) {
        if (!UserService.isInvalidUser(user)) {
            return "redirect:/";
        }
        return "user/login";
    }
}
