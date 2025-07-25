package com.dev.rebook.dtos.dashboard;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DashboardDto {
    private List<ProviderStatsDto> providerStats;
    private List<GenderStatsDto> genderStats;
    private List<AgeGroupStatsDto> ageGroupStats;
    private List<DailyUserRegisterStatsDto> dailyUserRegisterStats;
    private List<DailyReviewRegisterStatsDto> dailyReviewRegisterStats;
}
