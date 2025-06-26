package com.dev.rebook.controllers;

import com.dev.rebook.services.uesr.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getInfo() {
        return "user/info";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getRegister(@RequestParam(value = "t", required = false) String t,
                              HttpSession session, Model model) {
        if (t != null && t.equals("n")) {
            session.removeAttribute("oauthUser");
        }
        OAuth2User oauthUser = (OAuth2User) session.getAttribute("oauthUser");
        if (oauthUser != null) {
            model.addAttribute("email", oauthUser.getAttribute("email"));
            model.addAttribute("provider", oauthUser.getAttribute("provider"));
            model.addAttribute("name", oauthUser.getAttribute("name"));
        }
        return "user/register"; // 템플릿 파일
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getLogin() {
        return "user/login";
    }
}
