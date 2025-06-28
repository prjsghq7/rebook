package com.dev.rebook.entities;

import lombok.*;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class BookEntity {
    private String id;
    private String title;
    private String link;
    private String author;
    private LocalDate pubDate;
    private String description;
    private int priceSales;
    private String mallType;
    private String cover;
    private String publisher;
    private boolean adult;
}