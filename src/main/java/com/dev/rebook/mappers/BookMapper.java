package com.dev.rebook.mappers;

import com.dev.rebook.entities.BookEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookMapper {
    // 책 추가
    int insert(@Param(value = "book") BookEntity book);

    // DB에 저장된 책있는지 확인
    int selectCountById(@Param("id") String id);

    // DB에 저장된 단일 책 검색
    BookEntity selectById(@Param("id") String id);

    // DB에 저장된 전체 책 검색
    BookEntity[] selectAll();

    // 조회수 + 리뷰 수 기준 인기 도서 (알라딘 베스트셀러 대체)
    BookEntity[] selectMostPopular(@Param("limit") int limit);

    // 출간일 최신순 도서 (알라딘 신간 대체)
    BookEntity[] selectNewest(@Param("limit") int limit);

    int increaseView(@Param("id") String id);
}
