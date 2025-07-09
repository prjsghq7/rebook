package com.dev.rebook.entities;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class ReviewEntity {
    private int id;
    private int userId;
    private String bookId;
    private int scope;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean isDeleted;
}
