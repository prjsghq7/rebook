package com.dev.rebook.controlleradvices;

import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.services.BookService;
import com.dev.rebook.services.uesr.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;

@ControllerAdvice
public class CommonControllerAdvice {
    @ModelAttribute
    public void addHeaderAttribute(HttpServletRequest request,
                                   @SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                                   Model model) {
        if (request.getMethod().equals("GET")) {
            model.addAttribute("signedUser", signedUser);
        }
    }
}

