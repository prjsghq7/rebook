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
    private int sortType = 0;
    private String sort = "DESC";
    private Boolean adult = false;
    private Boolean mine = false;
}
