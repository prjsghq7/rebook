package com.dev.rebook.mappers;

import com.dev.rebook.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insert(@Param("user") UserEntity user);

    int update(@Param("user") UserEntity user);

    UserEntity selectLocalUserByContact(@Param(value = "contactMvno") String contactMvno,
                                        @Param(value = "contactFirst") String contactFirst,
                                        @Param(value = "contactSecond") String contactSecond,
                                        @Param(value = "contactThird") String contactThird);

    UserEntity selectLocalUserByEmail(@Param(value = "email") String email);

    UserEntity selectUserByProviderId(@Param("provider") String provider, @Param("providerId") String providerId);

    int selectLocalUserCountByEmail(@Param(value = "email") String email);

    int selectCountByEmail(@Param(value = "email") String email);

    int selectCountByNickname(@Param(value = "nickname") String nickname);

    int selectCountByContact(@Param(value = "contactFirst") String contactFirst,
                             @Param(value = "contactSecond") String contactSecond,
                             @Param(value = "contactThird") String contactThird);
}
