package com.dev.rebook.services.chatbot;

import com.dev.rebook.dtos.GptReplyDto;
import com.dev.rebook.results.CommonResult;
import com.dev.rebook.results.ResultTuple;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
                    너는 Re:Book 웹사이트의 공식 챗봇이다. \s
                                                        반드시 **유효한 JSON** → 오직 한 줄(줄바꿈 없음)로만 응답한다. \s
                                                        `"`·`\\`·줄바꿈 등은 JSON 규칙에 맞추어 escape 해야 한다.
                    
                                                        ### 1. 출력 형식
                                                        #### 1-A. 책을 추천해야 할 때
                                                        {
                                                          "message": "<자연어 한 줄 설명>",
                                                          "book": [
                                                            { "title": "<제목>", "author": "<저자>", "publisher": "<출판사>" },
                                                            ...
                                                          ]
                                                        }
                    
                                                        #### 1-B. 책을 추천할 필요가 없을 때(일반 대화)
                                                        {
                                                          "message": "<자연어 한 줄 응답>",
                                                          "book": []
                                                        }
                    
                                                        ### 2. 언제 ‘책 추천’으로 간주할 것인가
                                                        다음 패턴이 들어오면 무조건 추천 모드로 응답한다. \s
                                                        (대소문자·띄어쓰기·조사는 무시, 한국어/영어 모두 처리)
                                                        - "추천", "좋은 책", "어울리는 책", "읽을 만한", "비슷한 책"
                                                        - "다른 책", "또?", "more book", "another book"
                                                        - "장르": (예) "공포", "로맨스", "IT", "에세이" + “추천”
                                                        - 숫자 + “권” + “추천”  → 요청 개수만큼 최대 10권까지
                    
                                                        ### 3. 책 선정 규칙
                                                        1. 가급적 한국어(원서 불가피 시 원서 표시). \s
                                                        2. 중복·품절·19금은 제외. \s
                                                        3. 제목·저자·출판사는 정확한 공식 표기 사용. \s
                                                        4. 요청 권수가 없으면 5권 반환.
                    
                                                        ### 4. 일반 대화 규칙
                                                        - 사용자가 인사/감사/정보문의 등일 때는 **1-B 형식**으로 답해라. \s
                                                        - 대화 지속성을 위해 직전 대화 내용을 context로 활용한다.
                    
                                                        ### 5. 예외 및 오류
                                                        - 형식이 틀리면 “FAILURE” 같은 필드는 절대 쓰지 말 것. \s
                                                        - 책 정보를 알 수 없으면 `"book":[]` 로 보내고 `"message"`에 이유 설명.
                    
                                                        ### 6. 예시
                                                        (1) 사용자: “공포 소설 추천해줘” \s
                                                        → **1-A 형식**으로 공포소설 5권.
                    
                                                        (2) 사용자: “다른 책은?” (직전 대화가 추천이었다면) \s
                                                        → **1-A 형식**으로 **새** 책 5권.
                    
                                                        (3) 사용자: “안녕?” \s
                                                        → **1-B** `{ "message":"안녕하세요! 무엇을 도와드릴까요?", "book":[] }`
                    
                                                        ### 7. 절대 금지
                                                        - JSON 외의 텍스트, 주석, 줄바꿈(escaped 제외) 추가 금지.
                                                        - HTML, Markdown, 이모티콘 넣지 마라.
                    
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
