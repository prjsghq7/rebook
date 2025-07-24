package com.dev.rebook.dtos.dashboard;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GenderStatsDto {
    private String gender;
    private int userCount;
}
