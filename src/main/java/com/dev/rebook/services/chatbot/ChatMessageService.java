package com.dev.rebook.services.chatbot;

import com.dev.rebook.dtos.GptReplyDto;
import com.dev.rebook.entities.BookEntity;
import com.dev.rebook.entities.ChatMessageEntity;
import com.dev.rebook.entities.ChatRoomEntity;
import com.dev.rebook.entities.UserEntity;
import com.dev.rebook.mappers.ChatMessageMapper;
import com.dev.rebook.mappers.ChatRoomMapper;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.BookService;
import com.dev.rebook.services.uesr.UserService;
import com.dev.rebook.vos.SearchVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class ChatMessageService {

    private final ChatRoomMapper chatRoomMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final GPTService gptService;
    private ObjectMapper objectMapper = new ObjectMapper();
    private final BookService bookService;


    @Autowired
    public ChatMessageService(ChatRoomMapper chatRoomMapper, ChatMessageMapper chatMessageMapper, GPTService gptService, Dialect dialect, BookService bookService) {
        this.chatRoomMapper = chatRoomMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.gptService = gptService;
        this.bookService = bookService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // 채팅 메세지 등록 사용자 챗봇 둘 다
    public ResultTuple<ChatMessageEntity[]> registerChat(UserEntity signedUser, ChatMessageEntity chat) {
        if (UserService.isInvalidUser(signedUser)) {
            return ResultTuple.<ChatMessageEntity[]>builder()
                    .result(CommonResult.FAILURE_SESSION_EXPIRED)
                    .build();
        }
        if (chat.getChatRoomId() <= 0) {
            return ResultTuple.<ChatMessageEntity[]>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }

        ChatRoomEntity dbChatRoom = chatRoomMapper.selectChatRoomById(chat.getChatRoomId(), signedUser.getId());
        if (dbChatRoom == null || dbChatRoom.isDeleted()) {
            return ResultTuple.<ChatMessageEntity[]>builder().result(CommonResult.FAILURE).build();
        }

        // 사용자 메세지 저장
        chat.setSender(ChatMessageEntity.SenderType.user.toString());
        chat.setMessageType(ChatMessageEntity.MessageType.text.toString());
        chat.setCreatedAt(LocalDateTime.now());
        chat.setDeleted(false);

        int inserted = this.chatMessageMapper.insertChatMessage(chat);
        if (inserted <= 0) {
            return ResultTuple.<ChatMessageEntity[]>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
        // gpt응답 가져오기
        ResultTuple<GptReplyDto> botReply = gptService.getReply(chat.getMessage());
        if (botReply.getResult() != CommonResult.SUCCESS || botReply.getPayload() == null) {
            return ResultTuple.<ChatMessageEntity[]>builder().result(CommonResult.FAILURE).build();
        }
        // gpt 응답 저장
        GptReplyDto dto = botReply.getPayload();

        List<BookEntity> dbBookList = new ArrayList<>();

        if (dto.getBook() != null && !dto.getBook().isEmpty()) {
            for (GptReplyDto.BookDto bookDto : dto.getBook()) {
                try {
                    SearchVo searchVo = new SearchVo();
                    searchVo.setSearchType("title");
                    searchVo.setSearchTarget("book");
                    searchVo.setSort("accuracy");

                    // 알라딘 API 호출 -> 내부에서 insert 혹은 select
                    ResultTuple<BookEntity[]> r = this.bookService.searchBooksFromAladin(false, bookDto.getTitle(), searchVo);

                    // 검색 성공 시 첫 권을 DB 리스트에 추가
                    if (r.getResult() == CommonResult.SUCCESS && r.getPayload() != null && r.getPayload().length > 0) {
                        dbBookList.add(r.getPayload()[0]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String payloadJson = null;

        try {
            payloadJson = objectMapper.writeValueAsString(dbBookList);

//            objectMapper java객체를 json 문자열로 바꿔주는것
//                    또는 json 문자열을 java 객체로 바꿀수도있음 직렬화, 역직렬화
//
        } catch (Exception e) {
            e.printStackTrace(); // 에러 확인용
        }
        ChatMessageEntity botMessage = ChatMessageEntity.builder()
                .chatRoomId(chat.getChatRoomId())
                .sender(ChatMessageEntity.SenderType.bot.toString())
                .messageType(ChatMessageEntity.MessageType.bookRecommendation.toString())
                .message(dto.getMessage())
                .payload(payloadJson)
                .createdAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
        int insertChatMessageSuccess = this.chatMessageMapper.insertChatMessage(botMessage);

        return ResultTuple.<ChatMessageEntity[]>builder()
                .result(insertChatMessageSuccess > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE)
                .payload(new ChatMessageEntity[]{chat, botMessage})
                .build();
    }

    public ResultTuple<ChatMessageEntity[]> getMessageByRoomId(UserEntity signedUser, int roomId) {
        if (UserService.isInvalidUser(signedUser)) {
            return ResultTuple.<ChatMessageEntity[]>builder().result(CommonResult.FAILURE_SESSION_EXPIRED).build();
        }
        ChatRoomEntity dbRoom = this.chatRoomMapper.selectChatRoomById(roomId, signedUser.getId());
        if (dbRoom == null || dbRoom.isDeleted() || !dbRoom.getUserId().equals(signedUser.getId())) {
            return ResultTuple.<ChatMessageEntity[]>builder().result(CommonResult.FAILURE).build();
        }
        if (roomId < 1) {
            return ResultTuple.<ChatMessageEntity[]>builder().result(CommonResult.FAILURE).build();
        }
        ChatMessageEntity[] chatMessage = this.chatMessageMapper.selectedMessagesByChatRoomId(roomId);

        return ResultTuple.<ChatMessageEntity[]>builder()
                .result(CommonResult.SUCCESS)
                .payload(chatMessage)
                .build();
    }
}