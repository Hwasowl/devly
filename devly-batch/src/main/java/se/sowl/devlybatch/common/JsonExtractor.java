package se.sowl.devlybatch.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonExtractor {
    private final ObjectMapper objectMapper;

    public boolean isInvalidJson(String json) {
        return json == null || json.isEmpty();
    }

    public String extractField(String content, String fieldName) {
        int startIndex = content.indexOf(fieldName);
        if (startIndex == -1) return "";

        startIndex += fieldName.length();
        int endIndex = content.indexOf("\n", startIndex);
        if (endIndex == -1) {
            endIndex = content.length();
        }
        return content.substring(startIndex, endIndex).trim();
    }

    public String extractJsonArray(String content, String fieldName) {
        int startIndex = content.indexOf(fieldName);
        if (startIndex == -1) return "";

        startIndex += fieldName.length();
        int arrayStartIndex = content.indexOf("[", startIndex);
        if (arrayStartIndex == -1) return "";

        return findMatchingClosingBracket(content, arrayStartIndex);
    }

    public String findMatchingClosingBracket(String content, int arrayStartIndex) {
        int nestLevel = 1;
        int currentIndex = arrayStartIndex + 1;

        while (nestLevel > 0 && currentIndex < content.length()) {
            char c = content.charAt(currentIndex);
            if (c == '[') nestLevel++;
            else if (c == ']') nestLevel--;
            currentIndex++;
        }

        if (nestLevel == 0) {
            return content.substring(arrayStartIndex, currentIndex).trim();
        }
        return "";
    }

    public List<String> parseListString(String labelsJson) throws IOException {
        return objectMapper.readValue(labelsJson, new TypeReference<>() {});
    }

    public List<Map<String, String>> parseStringMap(String changedFilesJson) throws IOException {
        return objectMapper.readValue(changedFilesJson, new TypeReference<>() {});
    }
}
