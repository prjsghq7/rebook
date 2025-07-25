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

    public List<ProviderStatsDto> getUserProviderStats() {
        return this.userService.getProviderStats();
    }

    public List<GenderStatsDto> getUserGenderStats() {
        return this.userService.getGenderStats();
    }

    public List<AgeGroupStatsDto> getUserAgeGroupStats() {
        return this.userService.getAgeGroupStats();
    }

    public List<DailyUserRegisterStatsDto> getDailUserRegisterStats() {
        return this.userService.getDailyUserRegisterStats();
    }

    public List<DailyReviewRegisterStatsDto> getDailyReviewRegisterStats() {
        return this.reviewService.getDailyReviewRegisterStats();
    }

    public DashboardDto getDashBoardAll() {
        List<ProviderStatsDto> providerStats = this.userService.getProviderStats();
        List<GenderStatsDto> genderStats = this.userService.getGenderStats();
        List<AgeGroupStatsDto> ageGroupStats = this.userService.getAgeGroupStats();
        List<DailyUserRegisterStatsDto> dailyUserStats = this.userService.getDailyUserRegisterStats();
        List<DailyReviewRegisterStatsDto> dailyReviewStats = this.reviewService.getDailyReviewRegisterStats();

        return DashboardDto.builder()
                .providerStats(providerStats)
                .genderStats(genderStats)
                .ageGroupStats(ageGroupStats)
                .dailyUserRegisterStats(dailyUserStats)
                .dailyReviewRegisterStats(dailyReviewStats)
                .build();
    }


}