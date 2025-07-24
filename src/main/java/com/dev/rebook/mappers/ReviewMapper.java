package com.dev.rebook.mappers;

import com.dev.rebook.dtos.PopularBookDto;
import com.dev.rebook.dtos.RecentReviewDto;
import com.dev.rebook.dtos.ReviewPageItemDto;
import com.dev.rebook.dtos.ReviewSummaryDto;
import com.dev.rebook.dtos.ReviewWithProfileDto;
import com.dev.rebook.entities.ReviewEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.vos.ReviewPageButtonVo;
import com.dev.rebook.vos.ReviewPageVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewMapper {
    ReviewEntity selectReviewsById(@Param("id") int id);

    ReviewWithProfileDto[] selectReviewsByBookId(@Param("bookId") String bookId);

    RecentReviewDto[] selectRecentReviews(@Param("userId") int userId, int limit);

    ReviewSummaryDto selectReviewSummary(@Param("userId") int userId);

    int selectCountByUserIdAndBookId(@Param("userId") int userId, @Param("bookId") String bookId);

    int insert(@Param("review") ReviewEntity review);

    int update(@Param("review") ReviewEntity review);

    PopularBookDto[] selectPopularUserBooks();

    int selectCountReview();

    ReviewPageItemDto[] selectReviewAll(@Param("user") UserEntity user,
                                        @Param("reviewPageVo") ReviewPageVo reviewPageVo,
                                        @Param("reviewPageButtonVo") ReviewPageButtonVo reviewPageButtonVo);
}
