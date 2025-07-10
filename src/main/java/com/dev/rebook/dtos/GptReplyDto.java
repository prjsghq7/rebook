package com.dev.rebook.dtos;

import lombok.Data;

import java.util.List;

@Data
public class GptReplyDto {
    private String message;
    private List<BookDto> book;

    @Data
    public static class BookDto {
        private String title;
        private String author;
        private String publisher;
    }
}
