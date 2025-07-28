package com.dev.rebook.dtos.dashboard;

import com.dev.rebook.entities.DailyLoginStatsEntity;
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
    private List<DailyLoginStatsEntity> dailyUserLoginStats;
    private List<DailyUserRegisterStatsDto> dailyUserRegisterStats;
    private List<DailyReviewRegisterStatsDto> dailyReviewRegisterStats;
}
