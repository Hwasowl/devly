package se.sowl.devlybatch.common.gpt;

import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.gpt.exception.GPTContentProcessingException;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

@Component
public class GptResponseValidator {
    public void validateResponse(GPTResponse response) {
        if (response == null) {
            throw new GPTContentProcessingException("GPT response is null");
        }

        String content = response.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new GPTContentProcessingException("GPT response content is empty");
        }
    }

    public void validateEntries(String[] entries) {
        if (entries.length == 0 || (entries.length == 1 && entries[0].trim().isEmpty())) {
            throw new GPTContentProcessingException("No entries found in GPT response");
        }
    }
}
