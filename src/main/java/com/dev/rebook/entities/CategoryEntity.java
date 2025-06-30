package com.dev.rebook.entities;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class CategoryEntity {
    private String id;
    private String displayText;
    private int orderNo;
}
