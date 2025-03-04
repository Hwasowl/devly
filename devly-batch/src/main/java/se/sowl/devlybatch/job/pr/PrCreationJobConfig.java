package se.sowl.devlybatch.job.pr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlybatch.job.pr.utils.PrContentProcessor;
import se.sowl.devlybatch.job.pr.utils.PrPromptManager;
import se.sowl.devlybatch.service.StudyService;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.repository.PrRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PrCreationJobConfig {

    private final StudyService studyService;
    private final PrRepository prRepository;
    private final GPTClient gptClient;
    private final PrContentProcessor prContentProcessor;
    private final PrPromptManager prPromptManager;

    @Bean
    public Job prCreationJob(JobRepository jobRepository, Step createPrStep) {
        return new JobBuilder("createPrJob", jobRepository)
            .start(createPrStep)
            .build();
    }

    @Bean
    public Step createPrStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("createPrStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                createTodayPrStudies();
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    private void createTodayPrStudies() {
        List<Study> todayStudies = studyService.getTodayStudiesOf(StudyTypeEnum.PULL_REQUEST.getId());
        for (Study study : todayStudies) {
            GPTResponse response = getPrResponse(study);
            savePrOf(study, response);
            log.info("Created Pr for study {}", study.getId());
        }
    }

    private GPTResponse getPrResponse(Study study) {
        String prGeneratePrompt = createPrGeneratePrompt(study);
        return gptClient.generate(prContentProcessor.createGPTRequest(prGeneratePrompt));
    }

    private String createPrGeneratePrompt(Study study) {
        List<String> recentTitles = prRepository.findPrsByCreatedAtAfter(LocalDateTime.now().minusDays(7))
            .stream().map(Pr::getTitle).collect(Collectors.toList());
        return generatePrompt(study.getDeveloperTypeId(), recentTitles);
    }

    // TODO: 동일한 작업이 반복된다. knowledge 또한 같은 구조로 구현될 경우 공통 로직을 분리해보자.
    private void savePrOf(Study study, GPTResponse response) {
        List<Pr> prs = prContentProcessor.parseGPTResponse(response, study.getId());
        prRepository.saveAll(prs);
    }

    private String generatePrompt(Long developerTypeId, List<String> excludeContents) {
        StringBuilder prompt = new StringBuilder();
        prPromptManager.addPrompt(developerTypeId, prompt);
        prPromptManager.addExcludePrompt(excludeContents, prompt);
        return prompt.toString();
    }
}

