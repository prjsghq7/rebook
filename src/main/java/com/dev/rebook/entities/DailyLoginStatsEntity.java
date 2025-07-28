package com.dev.rebook.entities;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "statDate")
public class DailyLoginStatsEntity {
    LocalDate statDate;
    int userCount;
}
