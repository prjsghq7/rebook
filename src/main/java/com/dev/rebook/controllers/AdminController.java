package com.dev.rebook.controllers;

import com.dev.rebook.dtos.dashboard.DashboardDto;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.services.ReviewService;
import com.dev.rebook.services.admin.DashboardService;
import com.dev.rebook.services.uesr.UserService;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {
    private final DashboardService dashboardService;

    public AdminController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getIndex(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser) {
        return "admin/index";
    }

    @RequestMapping(value = "/dashboard/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getDashboard(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser) {
        DashboardDto dashboardDto = this.dashboardService.getDashBoardAll();
        JSONObject response = new JSONObject();
        response.put("stats", dashboardDto);
        return response.toString();
    }
}
