package com.dev.rebook.vos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewPageButtonVo {
    private String keyword;
    private int sortType = 0;
    private Boolean sort = false;
    private Boolean adult = false;
    private Boolean mine = false;
}
