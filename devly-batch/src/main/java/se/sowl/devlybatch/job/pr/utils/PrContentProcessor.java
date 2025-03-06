package se.sowl.devlybatch.job.pr.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.gpt.GptContentProcessor;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.pr.repository.PrChangedFileRepository;
import se.sowl.devlydomain.pr.repository.PrLabelRepository;
import se.sowl.devlydomain.pr.repository.PrRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PrContentProcessor extends GptContentProcessor<Pr> {

    private final PrRepository prRepository;
    private final PrChangedFileRepository prChangedFileRepository;
    private final PrLabelRepository prLabelRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public PrContentProcessor(
        PrRepository prRepository,
        PrChangedFileRepository prChangedFileRepository,
        PrLabelRepository prLabelRepository) {
        this.prRepository = prRepository;
        this.prChangedFileRepository = prChangedFileRepository;
        this.prLabelRepository = prLabelRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void parseEntity(Long studyId, String entry, List<Pr> contents) {
        try {
            Pr pr = createPr(studyId, entry);
            contents.add(pr);

            processChangedFiles(pr.getId(), entry);
            processLabels(pr.getId(), entry);
        } catch (Exception e) {
            log.error("PR Parsing Error: {}\n{}", entry, e);
        }
    }

    private Pr createPr(Long studyId, String entry) {
        String title = extractField(entry, "제목:");
        String description = extractField(entry, "설명:");

        Pr pr = Pr.builder()
            .title(title)
            .description(description)
            .studyId(studyId)
            .build();

        return prRepository.save(pr);
    }

    private void processChangedFiles(Long prId, String entry) {
        String changedFilesJson = extractJsonArray(entry, "변경 파일:");
        if (isInvalidJson(changedFilesJson)) return;

        try {
            List<Map<String, String>> changedFiles = parseChangedFiles(changedFilesJson);
            saveChangedFiles(prId, changedFiles);
        } catch (Exception e) {
            log.error("Changed File Parsing Error: {}", e.getMessage(), e);
        }
    }

    private List<Map<String, String>> parseChangedFiles(String changedFilesJson) throws IOException {
        return objectMapper.readValue(changedFilesJson, new TypeReference<>() {});
    }

    private void saveChangedFiles(Long prId, List<Map<String, String>> changedFiles) {
        for (Map<String, String> file : changedFiles) {
            PrChangedFile changedFile = PrChangedFile.builder()
                .pullRequestId(prId)
                .fileName(file.get("fileName"))
                .language(file.get("language"))
                .content(file.get("content"))
                .build();
            prChangedFileRepository.save(changedFile);
        }
    }

    private void processLabels(Long prId, String entry) {
        String labelsJson = extractJsonArray(entry, "라벨:");
        if (isInvalidJson(labelsJson)) return;

        try {
            List<String> labels = parseLabels(labelsJson);
            saveLabels(prId, labels);
        } catch (Exception e) {
            log.error("Label Parsing Error: {}", e.getMessage(), e);
        }
    }

    private List<String> parseLabels(String labelsJson) throws IOException {
        return objectMapper.readValue(labelsJson, new TypeReference<>() {});
    }

    private void saveLabels(Long prId, List<String> labels) {
        for (String label : labels) {
            PrLabel prLabel = new PrLabel(prId, label);
            prLabelRepository.save(prLabel);
        }
    }

    private boolean isInvalidJson(String json) {
        return json == null || json.isEmpty();
    }

    private String extractField(String content, String fieldName) {
        int startIndex = content.indexOf(fieldName);
        if (startIndex == -1) return "";

        startIndex += fieldName.length();
        int endIndex = content.indexOf("\n", startIndex);
        if (endIndex == -1) {
            endIndex = content.length();
        }
        return content.substring(startIndex, endIndex).trim();
    }

    private String extractJsonArray(String content, String fieldName) {
        int startIndex = content.indexOf(fieldName);
        if (startIndex == -1) return "";

        startIndex += fieldName.length();
        int arrayStartIndex = content.indexOf("[", startIndex);
        if (arrayStartIndex == -1) return "";

        return findMatchingClosingBracket(content, arrayStartIndex);
    }

    private String findMatchingClosingBracket(String content, int arrayStartIndex) {
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
}
