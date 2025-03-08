package se.sowl.devlybatch.job.pr.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.JsonExtractor;
import se.sowl.devlybatch.common.gpt.GptContentProcessor;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.pr.repository.PrChangedFileRepository;
import se.sowl.devlydomain.pr.repository.PrCommentRepository;
import se.sowl.devlydomain.pr.repository.PrLabelRepository;
import se.sowl.devlydomain.pr.repository.PrRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrContentProcessor extends GptContentProcessor<Pr> {
    private final PrRepository prRepository;
    private final PrChangedFileRepository prChangedFileRepository;
    private final PrLabelRepository prLabelRepository;
    private final PrCommentRepository prCommentRepository;
    private final JsonExtractor jsonExtractor;

    @Override
    protected void parseEntity(Long studyId, String entry, List<Pr> contents) {
        try {
            Pr pr = createPr(studyId, entry);
            contents.add(pr);
            processChangedFiles(pr.getId(), entry);
            processLabels(pr.getId(), entry);
            processComments(pr.getId(), entry);
        } catch (Exception e) {
            log.error("PR Parsing Error: {}\n{}", entry, e);
        }
    }

    private Pr createPr(Long studyId, String entry) {
        String title = jsonExtractor.extractField(entry, "제목:");
        String description = jsonExtractor.extractField(entry, "설명:");
        Pr pr = Pr.builder()
            .title(title)
            .description(description)
            .studyId(studyId)
            .build();
        return prRepository.save(pr);
    }

    private void processChangedFiles(Long prId, String entry) {
        String changedFilesJson = jsonExtractor.extractJsonArray(entry, "변경 파일:");
        if (jsonExtractor.isInvalidJson(changedFilesJson)) return;

        try {
            List<Map<String, String>> changedFiles = jsonExtractor.parseStringMap(changedFilesJson);
            saveChangedFiles(prId, changedFiles);
        } catch (Exception e) {
            log.error("Changed File Parsing Error: {}", e.getMessage(), e);
        }
    }

    private void saveChangedFiles(Long prId, List<Map<String, String>> changedFiles) {
        for (Map<String, String> file : changedFiles) {
            PrChangedFile changedFile = PrChangedFile.builder()
                .prId(prId)
                .fileName(file.get("fileName"))
                .language(file.get("language"))
                .content(file.get("content"))
                .build();
            prChangedFileRepository.save(changedFile);
        }
    }

    private void processLabels(Long prId, String entry) {
        String labelsJson = jsonExtractor.extractJsonArray(entry, "라벨:");
        if (jsonExtractor.isInvalidJson(labelsJson)) return;

        try {
            List<String> labels = jsonExtractor.parseListString(labelsJson);
            saveLabels(prId, labels);
        } catch (Exception e) {
            log.error("Label Parsing Error: {}", e.getMessage(), e);
        }
    }

    private void saveLabels(Long prId, List<String> labels) {
        for (String label : labels) {
            PrLabel prLabel = new PrLabel(prId, label);
            prLabelRepository.save(prLabel);
        }
    }

    private void processComments(Long prId, String entry) {
        String labelsJson = jsonExtractor.extractJsonArray(entry, "질문:");
        if (jsonExtractor.isInvalidJson(labelsJson)) return;
        try {
            List<String> comments = jsonExtractor.parseListString(labelsJson);
            saveComments(prId, comments);
        } catch (Exception e) {
            log.error("Comment Parsing Error: {}", e.getMessage(), e);
        }
    }

    private void saveComments(Long prId, List<String> comments) {
        List<PrComment> prComments = new ArrayList<>();
        String firstComment = "커밋 로그와 변경된 파일을 확인해 어떤 부분을 반영하고 개선한 PR인지 설명해주세요!";
        prComments.add(new PrComment(prId, 0L, firstComment));
        for (int i = 0; i < comments.size(); i++) {
            prComments.add(new PrComment(prId, (long) (i + 1), comments.get(i)));
        }
        prCommentRepository.saveAll(prComments);
    }
}
