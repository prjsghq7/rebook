package com.dev.rebook.mappers;

import com.dev.rebook.dtos.RecentReviewDto;
import com.dev.rebook.dtos.ReviewSummaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewMapper {
    RecentReviewDto[] selectRecentReviews(@Param("userId") int userId, int limit);

    ReviewSummaryDto selectReviewSummary(@Param("userId") int userId);
}
