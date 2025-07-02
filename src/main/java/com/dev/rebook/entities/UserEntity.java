package com.dev.rebook.entities;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class UserEntity {
    private int id;
    private String email;
    private String password;
    private String name;
    private String provider;
    private String providerId;
    private String profileImg;
    private String nickname;
    private String contactMvnoCode;
    private String contactFirst;
    private String contactSecond;
    private String contactThird;
    private LocalDate birth;
    private String addressPostal;
    private String addressPrimary;
    private String addressSecondary;
    private String gender;
    private LocalDate termAgreedAt;
    private String categoryId;
    private boolean isAdmin;
    private boolean isDeleted;
    private boolean isSuspended;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime lastSignedAt;
    private String lastSignedUa;
}
