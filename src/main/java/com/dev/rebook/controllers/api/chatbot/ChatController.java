package com.dev.rebook.controllers.api.chatbot;

import com.dev.rebook.dtos.ChatRoomWithLastMessageDto;
import com.dev.rebook.dtos.GptReplyDto;
import com.dev.rebook.entities.BookEntity;
import com.dev.rebook.entities.ChatMessageEntity;
import com.dev.rebook.entities.ChatRoomEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.mappers.ChatMessageMapper;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.chatbot.ChatMessageService;
import com.dev.rebook.services.chatbot.ChatRoomService;
import com.dev.rebook.services.chatbot.GPTService;
import com.dev.rebook.services.uesr.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/api/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatMessageService chatMessageService;
    private final GPTService gptService;


    @Autowired
    public ChatController(ChatRoomService chatRoomService, ChatMessageMapper chatMessageMapper, GPTService gptService, ChatMessageService chatMessageService, GPTService gptService1) {
        this.chatRoomService = chatRoomService;
        this.chatMessageService = chatMessageService;
        this.gptService = gptService1;
    }

    @RequestMapping(value = "/room", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResultTuple<ChatRoomEntity> getChatRoom(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                                                   @RequestParam(value = "room", required = false) ChatRoomEntity room) {
        if (signedUser == null || signedUser.isDeleted() || signedUser.isSuspended()) {
            return ResultTuple.<ChatRoomEntity>builder()
                    .result(CommonResult.FAILURE_SESSION_EXPIRED)
                    .build();
        }
        return this.chatRoomService.getChatRoomByRoomId(signedUser, room);
    }

    @RequestMapping(value = "/room/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResultTuple<ChatRoomEntity> postRegisterChatRoom(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                                                            ChatRoomEntity room
    ) {

        if (signedUser == null || signedUser.isDeleted() || signedUser.isSuspended()) {
            return ResultTuple.<ChatRoomEntity>builder()
                    .result(CommonResult.FAILURE_SESSION_EXPIRED)
                    .build();
        }
        return this.chatRoomService.registerChatRoom(signedUser, room);
    }

    @RequestMapping(value = "/room/lists", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResultTuple<ChatRoomWithLastMessageDto[]> getChatRoomList(HttpSession session) {
        UserEntity signedUser = (UserEntity) session.getAttribute("signedUser");
        if (signedUser == null || signedUser.isDeleted() || signedUser.isSuspended()) {
            return ResultTuple.<ChatRoomWithLastMessageDto[]>builder().result(CommonResult.FAILURE_SESSION_EXPIRED).build();
        }
        return this.chatRoomService.getChatRoomsLastMessage(signedUser);
    }

    @RequestMapping(value = "/room/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Result deleteRoom(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
                             @RequestParam(value = "room", required = false) ChatRoomEntity room) {
        if (signedUser == null || signedUser.isDeleted() || signedUser.isSuspended() || !signedUser.isAdmin()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        return this.chatRoomService.deletedChatRoom(signedUser, room);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ResultTuple<GptReplyDto>> sendMessage(@RequestParam(value = "message") String message) {

        ResultTuple<GptReplyDto> result = this.gptService.getReply(message);

        if (result.getResult() == CommonResult.SUCCESS) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultTuple.<GptReplyDto>builder()
                        .result(CommonResult.FAILURE)
                        .payload(null)
                        .build());
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ResultTuple<ChatMessageEntity[]>> handleChat(
            @SessionAttribute(value = "signedUser", required = false) UserEntity signedUser,
            @RequestBody ChatMessageEntity chat) {
        System.out.println("[IN CONTROLLER] room=" + chat.getChatRoomId() + ", msg=" + chat.getMessage());
        ResultTuple<ChatMessageEntity[]> result = chatMessageService.registerChat(signedUser, chat);
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/room/messages", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResultTuple<ChatMessageEntity[]> getMessage(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, @RequestParam(value = "roomId", required = false) int roomId) {
        return this.chatMessageService.getMessageByRoomId(signedUser, roomId);
    }
}
