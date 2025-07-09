package com.dev.rebook.services;

import com.dev.rebook.dtos.RecentReviewDto;
import com.dev.rebook.dtos.ReviewSummaryDto;
import com.dev.rebook.mappers.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private static final int INFO_RECENT_LIMIT = 5;

    private final ReviewMapper reviewMapper;

    @Autowired
    public ReviewService(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    public RecentReviewDto[] getRecentReviews(int userId) {
        return reviewMapper.selectRecentReviews(userId, INFO_RECENT_LIMIT);
    }

    public ReviewSummaryDto getReviewSummary(int userId) {
        return reviewMapper.selectReviewSummary(userId);
    }
}
