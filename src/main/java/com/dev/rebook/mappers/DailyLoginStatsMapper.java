package com.dev.rebook.mappers;

import com.dev.rebook.entities.DailyLoginStatsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyLoginStatsMapper {
    int incrementUserCount(@Param("statDate") LocalDate statDate);

    List<DailyLoginStatsEntity> selectDailyLoginStats(@Param(value = "from") LocalDate from,
                                                      @Param(value = "to") LocalDate to);
}
