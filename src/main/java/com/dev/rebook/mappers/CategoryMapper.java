package com.dev.rebook.mappers;

import com.dev.rebook.entities.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<CategoryEntity> selectAll();

    CategoryEntity selectById(@Param(value = "id") String id);

    int selectCountById(@Param(value = "id") String id);
}
