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
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.repository.PrRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PrCreationJobConfig {

    private final StudyRepository studyRepository;
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
                List<Study> todayStudies = getTodayStudies();
                for (Study study : todayStudies) {
                    String wordGeneratePrompt = createPrGeneratePrompt(study);
                    GPTResponse response = gptClient.generate(prContentProcessor.createGPTRequest(wordGeneratePrompt));
                    savePrOf(study, response);
                    log.info("Created Pr for study {}", study.getId());
                }
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    private List<Study> getTodayStudies() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return studyRepository.findByCreatedAtBetween(startOfDay, endOfDay);
    }

    private String createPrGeneratePrompt(Study study) {
        List<String> recentTitles = prRepository.findPrsByCreatedAtAfter(LocalDateTime.now().minusDays(7))
            .stream()
            .map(Pr::getTitle)
            .collect(Collectors.toList());
        return generatePrompt(study.getDeveloperTypeId(), recentTitles);
    }

    private void savePrOf(Study study, GPTResponse response) {
        List<Pr> words = prContentProcessor.parseGPTResponse(response, study.getId());
        prRepository.saveAll(words);
    }

    private String generatePrompt(Long developerTypeId, List<String> excludeContents) {
        StringBuilder prompt = new StringBuilder();
        prPromptManager.addPrompt(developerTypeId, prompt);
        prPromptManager.addExcludePrompt(excludeContents, prompt);
        return prompt.toString();
    }
}

