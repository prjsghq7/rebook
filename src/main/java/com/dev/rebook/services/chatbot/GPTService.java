package com.dev.rebook.services.chatbot;

import com.dev.rebook.dtos.GptReplyDto;
import com.dev.rebook.entities.BookEntity;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.Result;
import com.dev.rebook.results.ResultTuple;
import com.dev.rebook.services.BookService;
import com.dev.rebook.vos.SearchVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GPTService {
    @Value("${openai.apiKey}")
    private String apiKey;

    private final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final RestTemplate restTemplate = new RestTemplate();
    // 외부 http 서버에 요청을 보낼 수 있는 도구 post/get요청 보내고, 응답 받아올때 쓰는것
    private final ObjectMapper objectMapper = new ObjectMapper();
    // string 문자열로된 json을 실제 자바 객채처럼 다룰수있게해주는것.

    public ResultTuple<GptReplyDto> getReply(String userMessage) {

        if (userMessage == null || userMessage.isBlank()) {
            return ResultTuple.<GptReplyDto>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", """
                    너는 Re:Book이라는 도서 추천 챗봇이야. 사용자의 질문을 분석해서 **정확한 JSON 포맷**으로만 응답해. 반드시 아래 형식을 따르고, 문자열 내부에 `"`, `\n`, `\\` 등은 JSON 규칙에 맞게 escape 처리해.
                    
                    반드시 아래 두 가지 경우 중 하나로만 응답해:
                    책을 추천할 경우:
                    {
                      "message": "이런 책들을 추천드립니다",
                      "book": [
                        {
                          "title": "불편한 편의점",
                          "author": "김호연",
                          "publisher": "나무옆의자"
                        },
                        ...
                      ]
                    }
                    
                    그냥 대화일 경우:
                    {
                      "message": "챗봇의 일반적인 응답",
                      "book": []
                    }
                    
                    반드시 유효한 JSON만 출력하고, 설명/텍스트 절대 포함하지 마.
                    JSON 내부 줄바꿈은 \n 등 이스케이프 문법을 반드시 지켜줘.
                    """));

            messages.add(Map.of("role", "user", "content", userMessage));

            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            // http 요청을 위한 헤더설정.

            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

            // HttpEntity? http요청의 body, header를 하나의 객체로 묶는 클래스


            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, httpEntity, String.class);
            // 요청 ㄱㄱ

            if (!response.getStatusCode().is2xxSuccessful()) {
                return ResultTuple.<GptReplyDto>builder()
                        .result(CommonResult.FAILURE)
                        .build();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            // JsonNode : JSON 데이터를 xml 문서처럼 탐색할수있게 해주는것

            GptReplyDto gptReply = objectMapper.readValue(content, GptReplyDto.class);
            // content JSON문자열을 GptReplyDto 클래스로 변환해줠~

            return ResultTuple.<GptReplyDto>builder()
                    .result(CommonResult.SUCCESS)
                    .payload(gptReply)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultTuple.<GptReplyDto>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
    }

}
