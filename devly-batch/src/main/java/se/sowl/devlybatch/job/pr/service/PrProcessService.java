package se.sowl.devlybatch.job.pr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlybatch.job.pr.dto.PrWithRelations;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.repository.PrRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptPromptManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrProcessService {
    private final GPTClient gptClient;
    private final PrEntityParser prEntityParser;
    private final PrPersistenceService prPersistenceService;
    private final GptPromptManager gptPromptManager;
    private final PrRepository prRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPrStudies(Study study) {
        try {
            String prompt = createPrGeneratePrompt(study.getDeveloperType().getId(), study.getStudyType().getId());
            GPTResponse response = gptClient.generate(prEntityParser.createGPTRequest(prompt));
            List<PrWithRelations> parsedPrs = prEntityParser.parseGPTResponse(response, createParameters(study.getId()));
            for (PrWithRelations prWithRelations : parsedPrs) {
                prPersistenceService.savePrWithRelations(prWithRelations);
            }
            study.connect();
            log.info("Created PR for study {}", study.getId());
        } catch (Exception e) {
            log.error("Error processing PR for study ID: {}", study.getId(), e);
        }
    }

    private ParserArguments createParameters(Long studyId) {
        return new ParserArguments().add("studyId", studyId);
    }

    private String createPrGeneratePrompt(Long developerTypeId, Long studyTypeId) {
        StringBuilder prompt = new StringBuilder();
        List<String> recentTitles = prRepository.findPrsByCreatedAtAfter(LocalDateTime.now().minusDays(7))
            .stream().map(Pr::getTitle).toList();
        gptPromptManager.addExcludePrompt(recentTitles, prompt);
        gptPromptManager.addBasePrompt(developerTypeId, studyTypeId, prompt);
        return prompt.toString();
    }
}
