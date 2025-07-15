package com.dev.rebook.mappers;

import com.dev.rebook.dtos.RecentReviewDto;
import com.dev.rebook.dtos.ReviewSummaryDto;
import com.dev.rebook.entities.ReviewEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewMapper {
    RecentReviewDto[] selectRecentReviews(@Param("userId") int userId, int limit);

    ReviewSummaryDto selectReviewSummary(@Param("userId") int userId);

    int selectCountByUserIdAndBookId(@Param("userId") int userId, @Param("bookId") String bookId);

    int insert(@Param("review") ReviewEntity review);
}
