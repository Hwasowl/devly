package se.sowl.devlybatch.common.gpt;


import se.sowl.devlybatch.common.gpt.exception.GPTContentProcessingException;
import se.sowl.devlyexternal.client.gpt.dto.GPTRequest;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class GptContentProcessor<T> {
    public List<T> parseGPTResponse(GPTResponse response, Long studyId) {
        validateResponse(response);
        List<T> contents = new ArrayList<>();
        String content = response.getContent();
        try {
            String[] entries = content.split("---");
            validateEntries(entries);
            for (String entry : entries) {
                if (entry.trim().isEmpty()) continue;
                parseEntity(studyId, entry, contents);
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

    private void validateResponse(GPTResponse response) {
        if (response == null) {
            throw new GPTContentProcessingException("GPT response is null");
        }

        String content = response.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new GPTContentProcessingException("GPT response content is empty");
        }
    }

    private void validateEntries(String[] entries) {
        if (entries.length == 0 || (entries.length == 1 && entries[0].trim().isEmpty())) {
            throw new GPTContentProcessingException("No entries found in GPT response");
        }
    }

    private void handleException(Exception e) {
        if (e instanceof GPTContentProcessingException) {
            throw (GPTContentProcessingException) e;
        }
        throw new GPTContentProcessingException("Error processing GPT response: " + e.getMessage(), e);
    }

    abstract protected void parseEntity(Long studyId, String entry, List<T> contents);
}
