package com.dev.rebook.vos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SearchVo {
    private String searchType;
    private String searchTarget;
    private String sort;
}
