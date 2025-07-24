package com.dev.rebook.dtos.dashboard;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProviderStatsDto {
    private String provider;
    private int userCount;
}
