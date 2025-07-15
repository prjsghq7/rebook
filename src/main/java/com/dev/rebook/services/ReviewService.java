package com.dev.rebook.services;

import com.dev.rebook.dtos.RecentReviewDto;
import com.dev.rebook.dtos.ReviewSummaryDto;
import com.dev.rebook.entities.ReviewEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.mappers.ReviewMapper;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.services.uesr.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public Result insert(UserEntity signedUser, ReviewEntity review) {
        if (UserService.isInvalidUser(signedUser)) {
            if (signedUser == null || signedUser.isDeleted()) {
                return CommonResult.FAILURE_SESSION_EXPIRED;
            }
            return CommonResult.FAILURE_SUSPENDED;
        }

        if (this.reviewMapper.selectCountByUserIdAndBookId(signedUser.getId(), review.getBookId()) > 0) {
            return CommonResult.FAILURE_DUPLICATE;
        }

        // 추후 데이터 정규식 적용

        review.setUserId(signedUser.getId());
        review.setCreatedAt(LocalDateTime.now());
        review.setModifiedAt(LocalDateTime.now());
        review.setDeleted(false);

        return this.reviewMapper.insert(review) > 0 ?
                CommonResult.SUCCESS :
                CommonResult.FAILURE;
    }
}
