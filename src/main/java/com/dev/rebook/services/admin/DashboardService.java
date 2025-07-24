package com.dev.rebook.services.admin;

import com.dev.rebook.dtos.dashboard.*;
import com.dev.rebook.services.ReviewService;
import com.dev.rebook.services.uesr.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final UserService userService;
    private final ReviewService reviewService;

    @Autowired
    public DashboardService(UserService userService, ReviewService reviewService) {
        this.userService = userService;
        this.reviewService = reviewService;
    }

    public DashboardDto getDashBoardAll() {
        List<ProviderStatsDto> providerStats = this.userService.getProviderStats();
        List<GenderStatsDto> genderStats = this.userService.getGenderStats();
        List<DailyUserRegisterStatsDto> dailyUserStats = this.userService.getDailyUserRegisterStats();
        List<DailyReviewRegisterStatsDto> dailyReviewStats = this.reviewService.getDailyReviewRegisterStats();

        return DashboardDto.builder()
                .providerStats(providerStats)
                .genderStats(genderStats)
                .dailyUserRegisterStats(dailyUserStats)
                .dailyReviewRegisterStats(dailyReviewStats)
                .build();
    }


}