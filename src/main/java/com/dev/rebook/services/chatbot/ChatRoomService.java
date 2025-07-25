package com.dev.rebook.services.chatbot;

import com.dev.rebook.dtos.ChatRoomWithLastMessageDto;
import com.dev.rebook.entities.ChatRoomEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.mappers.ChatRoomMapper;
import com.dev.rebook.results.ChatbotResult;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.uesr.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatRoomService {

    private ChatRoomMapper chatRoomMapper;

    @Autowired
    public ChatRoomService(ChatRoomMapper chatRoomMapper) {
        this.chatRoomMapper = chatRoomMapper;
    }

    // 채팅방 등록
    public ResultTuple<ChatRoomEntity> registerChatRoom(UserEntity signedUser, ChatRoomEntity room) {
        if (UserService.isInvalidUser(signedUser)) {
            return ResultTuple.<ChatRoomEntity>builder()
                    .result(CommonResult.FAILURE_SESSION_EXPIRED)
                    .build();
        }
        room.setUserId(signedUser.getId());
        room.setCreatedAt(LocalDateTime.now());
        room.setModifiedAt(LocalDateTime.now());
        room.setDeleted(false);

        boolean insertChatRoomSuccess = chatRoomMapper.insertChatRoom(room) > 0;

        return ResultTuple.<ChatRoomEntity>builder()
                .result(insertChatRoomSuccess ? CommonResult.SUCCESS : CommonResult.FAILURE)
                .payload(insertChatRoomSuccess ? room : null)
                .build();
    }

    // 채팅방 목록 조회
    public ResultTuple<ChatRoomEntity[]> getChatRoomByUserId(UserEntity signedUser) {
        if (UserService.isInvalidUser(signedUser)) {
            return ResultTuple.<ChatRoomEntity[]>builder()
                    .result(CommonResult.FAILURE_SESSION_EXPIRED)
                    .build();
        }
        ChatRoomEntity[] rooms = chatRoomMapper.selectedChatRoomByUserId(signedUser.getId());


        return ResultTuple.<ChatRoomEntity[]>builder()
                .result(CommonResult.SUCCESS)
                .payload(rooms)
                .build();
    }

    // 채팅방 하나 단일 조회
    public ResultTuple<ChatRoomEntity> getChatRoomByRoomId(UserEntity signedUser, ChatRoomEntity room) {
        if (UserService.isInvalidUser(signedUser)) {
            return ResultTuple.<ChatRoomEntity>builder()
                    .result(CommonResult.FAILURE_SESSION_EXPIRED)
                    .build();
        }
        if (room == null || room.getId() == null || room.getId() <= 0) {
            return ResultTuple.<ChatRoomEntity>builder()
                    .result(ChatbotResult.FAILURE_ROOM_FIRED)
                    .build();
        }

        ChatRoomEntity dbRoom = this.chatRoomMapper.selectChatRoomById(room.getId(), signedUser.getId());
        if (dbRoom == null || dbRoom.isDeleted()) {
            return ResultTuple.<ChatRoomEntity>builder()
                    .result(ChatbotResult.FAILURE_ROOM_FIRED)
                    .build();
        }
        if (dbRoom.getUserId().equals(signedUser.getId())) {
            return ResultTuple.<ChatRoomEntity>builder()
                    .result(CommonResult.FAILURE_SESSION_EXPIRED)
                    .build();
        }
        return ResultTuple.<ChatRoomEntity>builder()
                .result(CommonResult.SUCCESS)
                .payload(dbRoom)
                .build();
    }

    // 채팅방 삭제
    public Result deletedChatRoom(UserEntity signedUser, Integer roomId) {
        if (UserService.isInvalidUser(signedUser)) {
            System.out.println("2");
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }

        if (roomId == null || roomId <= 0) {
            return ChatbotResult.FAILURE_ROOM_FIRED;
        }

        ChatRoomEntity dbRoom = this.chatRoomMapper.selectChatRoomById(roomId, signedUser.getId());

        if (dbRoom == null || dbRoom.isDeleted()) {
            return CommonResult.FAILURE_ABSENT;
        }

        if (!dbRoom.getUserId().equals(signedUser.getId())) {
            System.out.println("111111111111111111");
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        return chatRoomMapper.deleteChatRoom(dbRoom.getId(), signedUser.getId()) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }

    public ResultTuple<ChatRoomWithLastMessageDto[]> getChatRoomsLastMessage(UserEntity signedUser) {
        if (UserService.isInvalidUser(signedUser)) {
            return ResultTuple.<ChatRoomWithLastMessageDto[]>builder().result(CommonResult.FAILURE_SESSION_EXPIRED).build();
        }

        ChatRoomWithLastMessageDto[] rooms = chatRoomMapper.selectChatRoomsWithLastMessageByUserId(signedUser.getId());
        if (rooms == null) {
            return ResultTuple.<ChatRoomWithLastMessageDto[]>builder().result(CommonResult.FAILURE).build();
        }
        return ResultTuple.<ChatRoomWithLastMessageDto[]>builder()
                .result(CommonResult.SUCCESS)
                .payload(rooms)
                .build();
    }

}
