package com.dev.rebook.dtos.dashboard;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AgeGroupStatsDto {
    private String ageGroup;
    private int userCount;
}
