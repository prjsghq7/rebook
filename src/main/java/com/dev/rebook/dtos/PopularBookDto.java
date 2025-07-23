package com.dev.rebook.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PopularBookDto {
    private String bookId;
    private String author;
    private String title;
    private String cover;
    private int reviewCount;
    private int scope;
}
