package se.sowl.devlybatch.job.pr.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.JsonExtractor;
import se.sowl.devlybatch.job.pr.dto.PrWithRelations;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptEntityParser;
import se.sowl.devlyexternal.common.gpt.GptRequestFactory;
import se.sowl.devlyexternal.common.gpt.GptResponseValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PrEntityParser extends GptEntityParser<PrWithRelations> {
    private final JsonExtractor jsonExtractor;
    private final StudyRepository studyRepository;

    public PrEntityParser(
        JsonExtractor jsonExtractor,
        GptRequestFactory requestFactory,
        GptResponseValidator responseValidator,
        StudyRepository studyRepository
    ) {
        super(requestFactory, responseValidator, studyRepository);
        this.jsonExtractor = jsonExtractor;
        this.studyRepository = studyRepository;
    }

    @Override
    protected PrWithRelations parseEntity(ParserArguments parameters, String entry) {
        Long studyId = parameters.get("studyId", Long.class);
        try {
            String title = jsonExtractor.extractField(entry, "제목:");
            String description = jsonExtractor.extractField(entry, "설명:");
            Pr pr = Pr.builder().title(title).description(description).studyId(studyId).build();
            return getPrWithRelations(entry, pr);
        } catch (Exception e) {
            log.error("PR Parsing Error: {}", e.getMessage(), e);
            return null;
        }
    }

    private PrWithRelations getPrWithRelations(String entry, Pr pr) {
        List<PrChangedFile> changedFiles = parseChangedFiles(entry);
        List<PrLabel> labels = parseLabels(entry);
        List<PrComment> comments = parseComments(entry);
        return new PrWithRelations(pr, changedFiles, labels, comments);
    }

    private List<PrChangedFile> parseChangedFiles(String entry) {
        List<PrChangedFile> changedFiles = new ArrayList<>();
        String changedFilesJson = jsonExtractor.extractJsonArray(entry, "변경 파일:");
        if (jsonExtractor.isInvalidJson(changedFilesJson)) {
            return changedFiles;
        }
        try {
            List<Map<String, String>> filesData = jsonExtractor.parseStringMap(changedFilesJson);
            for (Map<String, String> file : filesData) {
                PrChangedFile changedFile = PrChangedFile.builder()
                    .fileName(file.get("fileName"))
                    .language(file.get("language"))
                    .content(file.get("content"))
                    .build();
                changedFiles.add(changedFile);
            }
        } catch (Exception e) {
            log.error("Changed File Parsing Error: {}", e.getMessage(), e);
        }

        return changedFiles;
    }

    private List<PrLabel> parseLabels(String entry) {
        List<PrLabel> prLabels = new ArrayList<>();
        String labelsJson = jsonExtractor.extractJsonArray(entry, "라벨:");
        if (jsonExtractor.isInvalidJson(labelsJson)) {
            return prLabels;
        }
        try {
            List<String> labels = jsonExtractor.parseListString(labelsJson);
            for (String label : labels) {
                prLabels.add(new PrLabel(null, label));
            }
        } catch (Exception e) {
            log.error("Label Parsing Error: {}", e.getMessage(), e);
        }

        return prLabels;
    }

    private List<PrComment> parseComments(String entry) {
        List<PrComment> prComments = new ArrayList<>();
        String commentsJson = jsonExtractor.extractJsonArray(entry, "질문:");

        String firstComment = "커밋 로그와 변경된 파일을 확인해 어떤 부분을 반영하고 개선한 PR인지 설명해주세요!";
        prComments.add(new PrComment(null, 0L, firstComment));

        if (jsonExtractor.isInvalidJson(commentsJson)) {
            return prComments;
        }

        try {
            List<String> comments = jsonExtractor.parseListString(commentsJson);
            for (int i = 0; i < comments.size(); i++) {
                prComments.add(new PrComment(null, (long) (i + 1), comments.get(i)));
            }
        } catch (Exception e) {
            log.error("Comment Parsing Error: {}", e.getMessage(), e);
        }

        return prComments;
    }
}
