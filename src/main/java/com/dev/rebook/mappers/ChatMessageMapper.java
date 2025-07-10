package com.dev.rebook.mappers;

import com.dev.rebook.entities.ChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    int insertChatMessage(@Param(value = "message") ChatMessageEntity message);

    ChatMessageEntity selectedMessagesByChatRoomId(@Param(value = "id") int chatRoomId);
}
