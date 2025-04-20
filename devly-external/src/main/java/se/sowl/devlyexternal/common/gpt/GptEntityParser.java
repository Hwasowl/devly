package se.sowl.devlyexternal.common.gpt;

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
        String[] entries = content.split("---");
        responseValidator.validateEntries(entries);
        parseEntities(parameters, entries, contents);
        if (contents.isEmpty()) {
            throw new GPTContentProcessingException("Failed to parse any valid entities from GPT response");
        }
    }

    private void parseEntities(ParserArguments parameters, String[] entries, List<T> contents) {
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            T entity = parseEntity(parameters, entry);
            if (entity != null) {
                contents.add(entity);
            }
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
