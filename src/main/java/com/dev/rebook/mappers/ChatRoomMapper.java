package com.dev.rebook.mappers;

import com.dev.rebook.dtos.ChatRoomWithLastMessageDto;
import com.dev.rebook.entities.ChatRoomEntity;
import com.dev.rebook.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatRoomMapper {
    int insertChatRoom(@Param(value = "room") ChatRoomEntity room);

    ChatRoomEntity selectChatRoomById(@Param("id") int id, @Param("userId") int userId);

    ChatRoomEntity[] selectedChatRoomByUserId(@Param(value = "userId") int user);

    int updateChatRoomTitle(@Param(value = "id") int id, @Param(value = "title") String title);

    int deleteChatRoom(@Param(value = "id") int id);

    ChatRoomWithLastMessageDto[] selectChatRoomsWithLastMessageByUserId(@Param("userId") int userId);




}
