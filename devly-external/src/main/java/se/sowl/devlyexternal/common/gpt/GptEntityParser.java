package se.sowl.devlyexternal.common.gpt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.sowl.devlyexternal.client.gpt.dto.GPTRequest;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.exception.GPTContentProcessingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public abstract class GptEntityParser<T> {
    private final GptRequestFactory requestFactory;
    private final GptResponseValidator responseValidator;
    private final ObjectMapper objectMapper;

    public List<T> parseGPTResponse(GPTResponse response, ParserArguments parameters) {
        responseValidator.validateResponse(response);
        List<T> contents = new ArrayList<>();
        String content = response.getContent();
        try {
            parseContents(parameters, content, contents);
            return contents;
        } catch (Exception e) {
            handleException(e);
            return Collections.emptyList();
        }
    }

    public GPTRequest createGPTRequest(String prompt) {
        return requestFactory.createGPTRequest(prompt);
    }

    private void parseContents(ParserArguments parameters, String content, List<T> contents) {
        try {
            String trimmedContent = content.trim();
            JsonNode jsonNode = objectMapper.readTree(trimmedContent);
            
            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    String entry = objectMapper.writeValueAsString(node);
                    T entity = parseEntity(parameters, entry);
                    if (entity != null) {
                        contents.add(entity);
                    }
                }
            } else {
                T entity = parseEntity(parameters, trimmedContent);
                if (entity != null) {
                    contents.add(entity);
                }
            }
            
            if (contents.isEmpty()) {
                throw new GPTContentProcessingException("Failed to parse any valid entities from GPT response");
            }
        } catch (Exception e) {
            throw new GPTContentProcessingException("Error processing GPT response: " + e.getMessage(), e);
        }
    }

    private void handleException(Exception e) {
        if (e instanceof GPTContentProcessingException) {
            throw (GPTContentProcessingException) e;
        }
        throw new GPTContentProcessingException("Error processing GPT response: " + e.getMessage(), e);
    }

    protected abstract T parseEntity(ParserArguments parameters, String entry);
}
