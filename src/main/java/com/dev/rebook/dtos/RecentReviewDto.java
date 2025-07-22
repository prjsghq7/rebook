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
public class RecentReviewDto {
    private int id;
    private String bookId;
    private String bookTitle;
    private String comment;
    private int scope;
    private LocalDate reviewDate;
}
