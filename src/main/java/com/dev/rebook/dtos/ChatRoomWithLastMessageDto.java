package com.dev.rebook.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRoomWithLastMessageDto {
    private int roomId;
    private LocalDateTime modifiedAt;

    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
