package se.sowl.devlybatch.job.pr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.repository.PrRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PrProcessService {
    private final PrRepository prRepository;
    private final GPTClient gptClient;
    private final PrContentProcessor prContentProcessor;
    private final PrPromptManager prPromptManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processPrStudies(Study study) {
        String prGeneratePrompt = createPrGeneratePrompt(study);
        GPTResponse response = gptClient.generate(prContentProcessor.createGPTRequest(prGeneratePrompt));
        savePrOf(study, response);
    }

    private String createPrGeneratePrompt(Study study) {
        List<String> recentTitles = prRepository.findPrsByCreatedAtAfter(LocalDateTime.now().minusDays(7))
            .stream().map(Pr::getTitle).collect(Collectors.toList());
        return generatePrompt(study.getDeveloperTypeId(), recentTitles);
    }

    private String generatePrompt(Long developerTypeId, List<String> excludeContents) {
        StringBuilder prompt = new StringBuilder();
        prPromptManager.addPrompt(developerTypeId, prompt);
        prPromptManager.addExcludePrompt(excludeContents, prompt);
        return prompt.toString();
    }

    private void savePrOf(Study study, GPTResponse response) {
        prContentProcessor.parseGPTResponse(response, study.getId());
    }
}
