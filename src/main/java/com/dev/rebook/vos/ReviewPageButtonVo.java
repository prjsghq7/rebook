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
    private int sort = 0;
    private Boolean mine = false;
}
