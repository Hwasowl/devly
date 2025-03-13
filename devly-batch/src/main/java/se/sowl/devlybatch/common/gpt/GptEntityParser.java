package se.sowl.devlybatch.common.gpt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.gpt.exception.GPTContentProcessingException;
import se.sowl.devlyexternal.client.gpt.dto.GPTRequest;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public abstract class GptEntityParser<T> {
    private final GptRequestFactory requestFactory;
    private final GptResponseValidator responseValidator;

    public List<T> parseGPTResponse(GPTResponse response, Long studyId) {
        responseValidator.validateResponse(response);
        List<T> contents = new ArrayList<>();
        String content = response.getContent();
        try {
            String[] entries = content.split("---");
            responseValidator.validateEntries(entries);
            for (String entry : entries) {
                if (entry.trim().isEmpty()) continue;
                T entity = parseEntity(studyId, entry);
                if (entity != null) {
                    contents.add(entity);
                }
            }
            if (contents.isEmpty()) {
                throw new GPTContentProcessingException("Failed to parse any valid entities from GPT response");
            }
            return contents;
        } catch (Exception e) {
            handleException(e);
            return Collections.emptyList();
        }
    }

    public GPTRequest createGPTRequest(String prompt) {
        return requestFactory.createGPTRequest(prompt);
    }

    private void handleException(Exception e) {
        if (e instanceof GPTContentProcessingException) {
            throw (GPTContentProcessingException) e;
        }
        throw new GPTContentProcessingException("Error processing GPT response: " + e.getMessage(), e);
    }

    protected abstract T parseEntity(Long studyId, String entry);
}
