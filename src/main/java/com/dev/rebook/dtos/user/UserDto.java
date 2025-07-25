package com.dev.rebook.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto {
    private int id;
    private String email;
    private String name;
    private String provider;
    private String nickname;
    private String gender;
    private String birth;
    private String contactMvnoCode;
    private String contactFirst;
    private String contactSecond;
    private String contactThird;
    private LocalDateTime createdAt;
    private LocalDateTime lastSignedAt;
    private boolean isAdmin;
    private boolean isDeleted;
    private boolean isSuspended;
}
