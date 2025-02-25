package se.sowl.devlybatch.common.gpt;


import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.util.ArrayList;
import java.util.List;

public abstract class GptParser<T> {

    public List<T> parseGPTResponse(GPTResponse response, Long studyId) {
        List<T> contents = new ArrayList<>();
        String content = response.getContent();
        String[] entries = content.split("---");
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            parseEntity(studyId, entry, contents);
        }
        return contents;
    }

    abstract protected void parseEntity(Long studyId, String entry, List<T> contents);
}
