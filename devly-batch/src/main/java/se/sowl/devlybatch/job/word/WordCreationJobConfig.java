package se.sowl.devlybatch.job.word;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.repository.WordRepository;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTRequest;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WordCreationJobConfig {

    private final StudyRepository studyRepository;
    private final WordRepository wordRepository;
    private final GPTClient gptClient;
    private final WordParser wordParser;
    private final WordPromptManager wordPromptManager;

    @Bean
    public Job wordCreationJob(JobRepository jobRepository, Step createWordsStep) {
        return new JobBuilder("wordCreationJob", jobRepository)
            .start(createWordsStep)
            .build();
    }

    @Bean
    public Step createWordsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("createWordsStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                List<Study> todayStudies = getTodayStudies();
                for (Study study : todayStudies) {
                    List<String> recentWords = wordRepository.findWordsByCreatedAtAfter(LocalDateTime.now().minusDays(7))
                        .stream().map(Word::getWord).collect(Collectors.toList());
                    GPTResponse response = gptClient.generate(createGPTRequest(study.getDeveloperTypeId(), recentWords));
                    saveWordsOf(study, response);
                    log.info("Created words for study {}", study.getId());
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

    private void saveWordsOf(Study study, GPTResponse response) {
        List<Word> words = wordParser.parseGPTResponse(response, study.getId());
        wordRepository.saveAll(words);
    }

    private GPTRequest createGPTRequest(Long developerTypeId, List<String> excludeWords) {
        String prompt = generatePrompt(developerTypeId, excludeWords);
        return GPTRequest.builder()
            .model("gpt-4")
            .messages(List.of(GPTRequest.Message.builder().role("user").content(prompt).build()))
            .temperature(0.7)
            .build();
    }

    private String generatePrompt(Long developerTypeId, List<String> excludeWords) {
        StringBuilder prompt = new StringBuilder();
        wordPromptManager.addPrompt(developerTypeId, prompt);
        wordPromptManager.addExcludePrompt(excludeWords, prompt);
        return prompt.toString();
    }
}
