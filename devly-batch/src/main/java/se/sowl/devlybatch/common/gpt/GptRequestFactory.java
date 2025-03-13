package se.sowl.devlybatch.common.gpt;

import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.gpt.exception.GPTContentProcessingException;
import se.sowl.devlyexternal.client.gpt.dto.GPTRequest;

import java.util.List;

@Component
public class GptRequestFactory {
    public GPTRequest createGPTRequest(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new GPTContentProcessingException("Prompt cannot be null or empty");
        }
        try {
            return GPTRequest.builder()
                .model("gpt-4")
                .messages(List.of(GPTRequest.Message.builder()
                    .role("user")
                    .content(prompt)
                    .build()))
                .temperature(0.7)
                .build();
        } catch (Exception e) {
            throw new GPTContentProcessingException("Failed to create GPT request: " + e.getMessage(), e);
        }
    }
}
