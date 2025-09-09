package se.sowl.devlybatch.job.pr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.sowl.devlybatch.job.pr.dto.PrWithRelations;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlyexternal.client.gpt.dto.ChangedFileDto;
import se.sowl.devlyexternal.client.gpt.dto.PrGPTResponse;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptEntityParser;
import se.sowl.devlyexternal.common.gpt.GptRequestFactory;
import se.sowl.devlyexternal.common.gpt.GptResponseValidator;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class PrEntityParser extends GptEntityParser<PrWithRelations> {
    private final ObjectMapper objectMapper;
    private final StudyRepository studyRepository;

    public PrEntityParser(
        ObjectMapper objectMapper,
        GptRequestFactory requestFactory,
        GptResponseValidator responseValidator,
        StudyRepository studyRepository) {
        super(requestFactory, responseValidator, objectMapper);
        this.objectMapper = objectMapper;
        this.studyRepository = studyRepository;
    }

    @Override
    protected PrWithRelations parseEntity(ParserArguments parameters, String entry) {
        Study study = studyRepository.findById(parameters.get("studyId", Long.class)).orElseThrow(
            () -> new IllegalArgumentException("Study not found with ID: " + parameters.get("studyId", Long.class))
        );
        try {
            PrGPTResponse prResponse = objectMapper.readValue(entry, PrGPTResponse.class);
            Pr pr = Pr.builder()
                .title(prResponse.getTitle())
                .description(prResponse.getDescription())
                .study(study)
                .build();
            return getPrWithRelations(prResponse, pr);
        } catch (Exception e) {
            log.error("PR Parsing Error: {}", e.getMessage(), e);
            return null;
        }
    }

    private PrWithRelations getPrWithRelations(PrGPTResponse prResponse, Pr pr) {
        List<PrChangedFile> changedFiles = parseChangedFiles(prResponse.getChangedFiles());
        List<PrLabel> labels = parseLabels(prResponse.getLabels());
        List<PrComment> comments = parseComments();
        return new PrWithRelations(pr, changedFiles, labels, comments);
    }

    private List<PrChangedFile> parseChangedFiles(List<ChangedFileDto> changedFilesDtos) {
        List<PrChangedFile> changedFiles = new ArrayList<>();
        if (changedFilesDtos == null) {
            return changedFiles;
        }
        try {
            for (ChangedFileDto fileDto : changedFilesDtos) {
                PrChangedFile changedFile = PrChangedFile.builder()
                    .fileName(fileDto.getFileName())
                    .language(fileDto.getLanguage())
                    .content(fileDto.getContent())
                    .build();
                changedFiles.add(changedFile);
            }
        } catch (Exception e) {
            log.error("Changed File Parsing Error: {}", e.getMessage(), e);
        }
        return changedFiles;
    }

    private List<PrLabel> parseLabels(List<String> labelStrings) {
        List<PrLabel> prLabels = new ArrayList<>();
        if (labelStrings == null) {
            return prLabels;
        }
        try {
            for (String label : labelStrings) {
                prLabels.add(new PrLabel(null, label));
            }
        } catch (Exception e) {
            log.error("Label Parsing Error: {}", e.getMessage(), e);
        }
        return prLabels;
    }

    private List<PrComment> parseComments() {
        List<PrComment> prComments = new ArrayList<>();
        String firstComment = "커밋 로그와 변경된 파일을 확인해 어떤 부분을 반영하고 개선한 PR인지 설명해주세요!";
        String secondComment = "코드 변경사항에 대한 설명을 추가해주세요.";
        String thirdComment = "테스트 코드도 함께 작성해주시면 좋겠습니다.";
        
        prComments.add(new PrComment(null, 0L, firstComment));
        prComments.add(new PrComment(null, 1L, secondComment));
        prComments.add(new PrComment(null, 2L, thirdComment));
        return prComments;
    }
}
