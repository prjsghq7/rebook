package com.dev.rebook.mappers;

import com.dev.rebook.dtos.dashboard.AgeGroupStatsDto;
import com.dev.rebook.dtos.dashboard.DailyUserRegisterStatsDto;
import com.dev.rebook.dtos.dashboard.GenderStatsDto;
import com.dev.rebook.dtos.dashboard.ProviderStatsDto;
import com.dev.rebook.dtos.user.UserDto;
import com.dev.rebook.entities.UserEntity;
import com.nimbusds.openid.connect.sdk.claims.Gender;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    int insert(@Param("user") UserEntity user);

    int update(@Param("user") UserEntity user);

    int editUser(UserDto dbUser);

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

    List<ProviderStatsDto> selectProviderStats();

    List<GenderStatsDto> selectGenderStats();

    List<AgeGroupStatsDto> selectAgeGroupStats();

    List<DailyUserRegisterStatsDto> selectDailyUserRegisterStats();

    List<UserDto> selectAllUser();

    UserDto selectUserById(@Param(value = "id") int id);

}
