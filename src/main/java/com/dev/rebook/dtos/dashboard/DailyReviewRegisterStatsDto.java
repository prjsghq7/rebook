package com.dev.rebook.dtos.dashboard;

import lombok.*;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DailyReviewRegisterStatsDto {
    private LocalDate date;
    private int reviewCount;
}
