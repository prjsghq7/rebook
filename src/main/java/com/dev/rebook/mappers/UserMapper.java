package com.dev.rebook.mappers;

import com.dev.rebook.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    public UserEntity selectUserByProviderId(@Param("provider") String provider, @Param("providerId") String providerId);
}
