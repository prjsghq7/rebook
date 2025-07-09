package com.dev.rebook.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewSummaryDto {
    private int totalCount;
    private double averageScope;
    private LocalDateTime lastReviewDate;
}
