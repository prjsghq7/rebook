package com.dev.rebook.controllers;

import com.dev.rebook.dtos.dashboard.*;
import com.dev.rebook.dtos.user.UserDto;
import com.dev.rebook.entities.ContactMvnoEntity;
import com.dev.rebook.entities.DailyLoginStatsEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.mappers.ContactMvnoMapper;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.admin.DashboardService;
import com.dev.rebook.services.uesr.UserService;
import com.dev.rebook.vos.ReviewPageButtonVo;
import com.dev.rebook.vos.ReviewPageVo;
import org.json.JSONObject;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
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

    @RequestMapping(value = "/dashboard/daily-user-login", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DailyLoginStatsEntity> getDailyUserLogin(@RequestParam(value = "from", required = false) LocalDate from,
                                                         @RequestParam(value = "to", required = false) LocalDate to) {
        return this.dashboardService.getDailUserLoginStats(from, to);
    }

    @RequestMapping(value = "/dashboard/daily-user-register", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DailyUserRegisterStatsDto> getDailyUserRegister(@RequestParam(value = "from", required = false) LocalDate from,
                                                                @RequestParam(value = "to", required = false) LocalDate to) {
        return this.dashboardService.getDailUserRegisterStats(from, to);
    }

    @RequestMapping(value = "/dashboard/daily-review-register", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DailyReviewRegisterStatsDto> getDailyReviewRegister(LocalDate from, LocalDate to) {
        return this.dashboardService.getDailyReviewRegisterStats(from, to);
    }

    @RequestMapping(value = "/dashboard/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DashboardDto getDashboardAll(@RequestParam(value = "from", required = false) LocalDate from,
                                        @RequestParam(value = "to", required = false) LocalDate to) {
        return this.dashboardService.getDashBoardAll(from, to);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getIndex(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser) {
        if (signedUser == null || !signedUser.isAdmin()) {
            return "redirect:/";
        }
        return "admin/index";
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getUserIndex(
            @SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            ReviewPageButtonVo reviewPageButtonVo,
            Model model) {

        ResultTuple<Pair<UserDto[], ReviewPageVo>> result = userService.getUserAll(signedUser, page);

        if (!result.getResult().equals(CommonResult.SUCCESS)) {
            return "redirect:/";
        }

        Pair<UserDto[], ReviewPageVo> data = result.getPayload();

        ContactMvnoEntity[] contactMvnos = userService.getContactMvnos();
        model.addAttribute("contactMvnos", contactMvnos);
        model.addAttribute("user", new UserDto());
        model.addAttribute("reviewPageButtonVo", reviewPageButtonVo);
        model.addAttribute("users", data.getFirst());
        model.addAttribute("reviewPageVo", data.getSecond());

        return "admin/user";
    }


    @RequestMapping(value = "/user/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResultTuple<List<UserDto>> getUserInfo(
            @SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
            @RequestParam(value = "page", defaultValue = "1") int page) {

        ResultTuple<Pair<UserDto[], ReviewPageVo>> result = userService.getUserAll(signedUser, page);

        // 실패 시 바로 반환
        if (!result.getResult().equals(CommonResult.SUCCESS)) {
            return ResultTuple.<List<UserDto>>builder()
                    .result(result.getResult())
                    .build();
        }

        // 성공이면 payload에서 Pair 꺼내서 List로 변환
        Pair<UserDto[], ReviewPageVo> data = result.getPayload();
        List<UserDto> payload = Arrays.asList(data.getFirst());

        return ResultTuple.<List<UserDto>>builder()
                .result(CommonResult.SUCCESS)
                .payload(payload)
                .build();
    }


    @RequestMapping(value = "/user/edit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResultTuple<Void> editUserInfo(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, UserDto userDto) {
        Result result = this.userService.editUserInfo(signedUser, userDto);
        JSONObject response = new JSONObject();
        response.put("result", result);
        return ResultTuple.<Void>builder().result(CommonResult.SUCCESS).build();
    }
}
