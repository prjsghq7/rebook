package com.dev.rebook.controllers;

import com.dev.rebook.dtos.dashboard.*;
import com.dev.rebook.dtos.user.UserDto;
import com.dev.rebook.entities.ContactMvnoEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.mappers.ContactMvnoMapper;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.results.ResultTuple;
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

import java.util.List;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {
    private final DashboardService dashboardService;
    private final UserService userService;
    private final ContactMvnoMapper contactMvnoMapper;

    public AdminController(DashboardService dashboardService, UserService userService, ContactMvnoMapper contactMvnoMapper) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.contactMvnoMapper = contactMvnoMapper;
    }

    @RequestMapping(value = "/dashboard/user-provider", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ProviderStatsDto> getDashboardUserProvider() {
        return this.dashboardService.getUserProviderStats();
    }

    @RequestMapping(value = "/dashboard/user-gender", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<GenderStatsDto> getDashboardUserGender() {
        return this.dashboardService.getUserGenderStats();
    }

    @RequestMapping(value = "/dashboard/user-age-group", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<AgeGroupStatsDto> getDashboardUserAgeGroup() {
        return this.dashboardService.getUserAgeGroupStats();
    }

    @RequestMapping(value = "/dashboard/daily-user-register", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DailyUserRegisterStatsDto> getDailyUserRegister() {
        return this.dashboardService.getDailUserRegisterStats();
    }

    @RequestMapping(value = "/dashboard/daily-review-register", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DailyReviewRegisterStatsDto> getDailyReviewRegister() {
        return this.dashboardService.getDailyReviewRegisterStats();
    }

    @RequestMapping(value = "/dashboard/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DashboardDto getDashboardAll() {
        return this.dashboardService.getDashBoardAll();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getIndex(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser) {
        if (signedUser == null || !signedUser.isAdmin()) {
            return "redirect:/";
        }
        return "admin/index";
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getUserIndex(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, Model model) {
        ContactMvnoEntity[] contactMvnos = userService.getContactMvnos();
        model.addAttribute("contactMvnos", contactMvnos);
        model.addAttribute("user", new UserDto());
        if (signedUser == null || !signedUser.isAdmin()) {
            return "redirect:/";
        }
        return "admin/user";
    }

    @RequestMapping(value = "/user/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResultTuple<List<UserDto>> getUserInfo(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser) {
        if (signedUser == null || !signedUser.isAdmin()) {
            return ResultTuple.<List<UserDto>>builder().result(CommonResult.FAILURE_SESSION_EXPIRED).build();
        }
        return this.userService.selectUserInfo(signedUser);
    }

    @RequestMapping(value = "/user/edit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Result editUserInfo(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, UserDto userDto) {
        if (signedUser == null || !signedUser.isAdmin()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        Result result = this.userService.editUserInfo(signedUser, userDto);
        JSONObject response = new JSONObject();
        response.put("result", result);
        System.out.println(result);
        return result;
    }
}
