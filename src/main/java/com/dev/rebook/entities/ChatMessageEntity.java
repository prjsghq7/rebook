package com.dev.rebook.entities;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class ChatMessageEntity {

    private int id;
    private int chatRoomId;
    private String sender;        // enum → String
    private String messageType;   // enum → String
    private String message;
    private String payload;
    private LocalDateTime createdAt;
    private boolean isDeleted;

    // enum은 그냥 남겨놔도 괜찮음. 필요하면 toString 쓸 수 있음.
    public enum SenderType {
        user("user"), bot("bot");
        private final String dbValue;
        SenderType(String dbValue) { this.dbValue = dbValue; }
        @Override public String toString() { return dbValue; }
    }

    public enum MessageType {
        text("text"), bookRecommendation("book_recommendation");
        private final String dbValue;
        MessageType(String dbValue) { this.dbValue = dbValue; }
        @Override public String toString() { return dbValue; }
    }

}
