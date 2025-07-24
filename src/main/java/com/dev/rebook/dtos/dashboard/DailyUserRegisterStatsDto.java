package com.dev.rebook.dtos.dashboard;

import lombok.*;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DailyUserRegisterStatsDto {
    private LocalDate date;
    private int userCount;
}
