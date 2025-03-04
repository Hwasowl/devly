package se.sowl.devlybatch.job.pr.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.gpt.GptContentProcessor;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.pr.repository.PrChangedFileRepository;
import se.sowl.devlydomain.pr.repository.PrLabelRepository;
import se.sowl.devlydomain.pr.repository.PrRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PrContentProcessor extends GptContentProcessor<Pr> {
    private final PrRepository prRepository;
    private final PrChangedFileRepository prChangedFileRepository;
    private final PrLabelRepository prLabelRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void parseEntity(Long studyId, String entry, List<Pr> prs) {
        try {
            String[] lines = entry.trim().split("\n");
            String title = lines[0].replace("제목: ", "").trim();
            String description = lines[1].replace("설명: ", "").trim();
            Pr pr = savePr(title, description);
            Long prId = pr.getId();
            saveChangedFiles(lines, prId);
            savePrLabels(lines, prId);
            prs.add(pr);
        } catch (Exception e) {
            log.error("Error parsing PR entry: {}", entry, e);
        }
    }

    private Pr savePr(String title, String description) {
        Pr pr = Pr.builder()
            .title(title)
            .description(description)
            .build();
        pr = prRepository.save(pr);
        return pr;
    }

    private void saveChangedFiles(String[] lines, Long prId) throws JsonProcessingException {
        String changedFilesLine = lines[2].replace("변경 파일: ", "").trim();
        JsonNode changedFilesNode = objectMapper.readTree(changedFilesLine);
        for (JsonNode fileNode : changedFilesNode) {
            PrChangedFile changedFile = PrChangedFile.builder()
                .pullRequestId(prId)
                .fileName(fileNode.get("fileName").asText())
                .language(fileNode.get("language").asText())
                .content(fileNode.get("content").asText())
                .build();
            prChangedFileRepository.save(changedFile);
        }
    }

    private void savePrLabels(String[] lines, Long prId) throws JsonProcessingException {
        String labelsLine = lines[3].replace("라벨: ", "").trim();
        JsonNode labelsNode = objectMapper.readTree(labelsLine);
        labelsNode.forEach(labelNode -> prLabelRepository.save(new PrLabel(prId, labelNode.asText())));
    }
}
