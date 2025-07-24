package com.dev.rebook.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewPageItemDto {
    private String bookId;
    private String bookTitle;
    private String comment;
    private String nickname;
    private String cover;
    private int scope;
//    private LocalDate reviewDate;
}
