package com.dev.rebook.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewWithProfileDto {
    private int id;
    private int userId;
    private String bookId;
    private String nickname;
    private String profileImg;
    private String comment;
    private int scope;
    private LocalDate reviewDate;
    private boolean hasPermission = false;
}
