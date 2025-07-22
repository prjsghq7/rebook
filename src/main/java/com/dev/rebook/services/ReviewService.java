package com.dev.rebook.services;

import com.dev.rebook.dtos.RecentReviewDto;
import com.dev.rebook.dtos.ReviewSummaryDto;
import com.dev.rebook.dtos.ReviewWithProfileDto;
import com.dev.rebook.entities.ReviewEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.mappers.ReviewMapper;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.results.review.DeleteResult;
import com.dev.rebook.results.review.ModifyResult;
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

    public ReviewEntity getReviewById(int id) {
        return reviewMapper.selectReviewsById(id);
    }

    public ReviewWithProfileDto[] getReviewsByBookId(String bookId, UserEntity signedUser) {
        ReviewWithProfileDto[] reviews = reviewMapper.selectReviewsByBookId(bookId);
        boolean isAdmin = false;
        int userId = 0;
        if (signedUser != null) {
            if (signedUser.isAdmin()) {
                isAdmin = true;
            }
            userId = signedUser.getId();
        }
        for (ReviewWithProfileDto review : reviews) {
            if (isAdmin || review.getUserId() == userId) {
                review.setHasPermission(true);
            }
        }
        return reviews;
    }

    public RecentReviewDto[] getRecentReviews(int userId) {
        return reviewMapper.selectRecentReviews(userId, INFO_RECENT_LIMIT);
    }

    public ReviewSummaryDto getReviewSummary(int userId) {
        return reviewMapper.selectReviewSummary(userId);
    }

    public Result delete(UserEntity signedUser, int id) {
        if (UserService.isInvalidUser(signedUser)) {
            if (signedUser == null || signedUser.isDeleted()) {
                return CommonResult.FAILURE_SESSION_EXPIRED;
            }
            return CommonResult.FAILURE_SUSPENDED;
        }

        ReviewEntity dbReview = reviewMapper.selectReviewsById(id);
        if (dbReview == null || dbReview.isDeleted()) {
            return CommonResult.FAILURE_ABSENT;
        }
        if (dbReview.getUserId() != signedUser.getId()) {
            return DeleteResult.FAILURE_NO_PERMISSION;
        }

        dbReview.setDeleted(true);
        dbReview.setModifiedAt(LocalDateTime.now());

        return this.reviewMapper.update(dbReview) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
    }

    public Result modify(UserEntity signedUser, ReviewEntity review) {
        if (UserService.isInvalidUser(signedUser)) {
            if (signedUser == null || signedUser.isDeleted()) {
                return CommonResult.FAILURE_SESSION_EXPIRED;
            }
            return CommonResult.FAILURE_SUSPENDED;
        }

        if (review.getScope() < 1 || review.getScope() > 5
                || review.getComment() == null || review.getComment().isEmpty()) {
            return CommonResult.FAILURE;
        }

        ReviewEntity dbReview = reviewMapper.selectReviewsById(review.getId());
        if (dbReview == null || dbReview.isDeleted()) {
            return CommonResult.FAILURE_ABSENT;
        }
        if (dbReview.getUserId() != signedUser.getId()) {
            return ModifyResult.FAILURE_NO_PERMISSION;
        }
        dbReview.setScope(review.getScope());
        dbReview.setComment(review.getComment());
        dbReview.setModifiedAt(LocalDateTime.now());

        return this.reviewMapper.update(dbReview) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
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

        if (review.getScope() < 1 || review.getScope() > 5
                || review.getComment() == null || review.getComment().isEmpty()) {
            return CommonResult.FAILURE;
        }

        review.setUserId(signedUser.getId());
        review.setCreatedAt(LocalDateTime.now());
        review.setModifiedAt(LocalDateTime.now());
        review.setDeleted(false);

        return this.reviewMapper.insert(review) > 0 ?
                CommonResult.SUCCESS :
                CommonResult.FAILURE;
    }
}
