package se.sowl.devlyexternal.client.gpt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.DiscussionGPTRequest;
import se.sowl.devlyexternal.client.gpt.dto.DiscussionGPTResponse;
import se.sowl.devlyexternal.client.gpt.dto.GPTRequest;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscussionGPTService {
    private final GPTClient gptClient;
    private final ObjectMapper objectMapper;
    
    public DiscussionGPTResponse evaluateAnswer(DiscussionGPTRequest request) {
        try {
            String prompt = request.buildPrompt();
            GPTRequest.Message message = GPTRequest.Message.builder()
                .role("user")
                .content(prompt)
                .build();
            
            GPTRequest gptRequest = GPTRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(message))
                .temperature(0.7)
                .build();
            
            GPTResponse gptResponse = gptClient.generate(gptRequest);
            String responseContent = gptResponse.getContent();
            
            return parseDiscussionResponse(responseContent);
        } catch (Exception e) {
            log.error("Failed to evaluate discussion answer", e);
            throw new RuntimeException("GPT 응답 처리 중 오류가 발생했습니다.", e);
        }
    }
    
    private DiscussionGPTResponse parseDiscussionResponse(String gptResponse) throws JsonProcessingException {
        try {
            String jsonResponse = extractJsonFromResponse(gptResponse);
            return objectMapper.readValue(jsonResponse, DiscussionGPTResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse GPT response: {}", gptResponse, e);
            return createDefaultResponse();
        }
    }
    
    private String extractJsonFromResponse(String response) {
        int startIndex = response.indexOf("{");
        int lastIndex = response.lastIndexOf("}");
        
        if (startIndex != -1 && lastIndex != -1 && startIndex < lastIndex) {
            return response.substring(startIndex, lastIndex + 1);
        }
        
        throw new RuntimeException("GPT 응답에서 JSON을 찾을 수 없습니다: " + response);
    }
    
    private DiscussionGPTResponse createDefaultResponse() {
        return new DiscussionGPTResponse(
            "답변을 평가하는 중 오류가 발생했습니다. 다시 시도해주세요.",
            5.0,
            "다음 질문을 생성하는 중 오류가 발생했습니다.",
            null
        );
    }
}