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
}
