package se.sowl.devlybatch.job.word;

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
import se.sowl.devlybatch.job.word.utils.WordContentProcessor;
import se.sowl.devlybatch.job.word.utils.WordPromptManager;
import se.sowl.devlybatch.service.StudyService;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.repository.WordRepository;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WordCreationJobConfig {

    private final StudyService studyService;
    private final WordRepository wordRepository;
    private final GPTClient gptClient;
    private final WordContentProcessor wordContentProcessor;
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
                createTodayWordStudies();
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    private void createTodayWordStudies() {
        List<Study> todayStudies = studyService.getTodayStudiesOf(StudyTypeEnum.WORD.getId());
        for (Study study : todayStudies) {
            GPTResponse response = getGptResponse(study);
            saveWordsOf(study, response);
            log.info("Created words for study {}", study.getId());
        }
    }

    private GPTResponse getGptResponse(Study study) {
        String wordGeneratePrompt = createWordGeneratePrompt(study);
        return gptClient.generate(wordContentProcessor.createGPTRequest(wordGeneratePrompt));
    }

    private String createWordGeneratePrompt(Study study) {
        List<String> recentWords = wordRepository.findWordsByCreatedAtAfter(LocalDateTime.now().minusDays(7))
            .stream().map(Word::getWord).collect(Collectors.toList());
        return generatePrompt(study.getDeveloperTypeId(), recentWords);
    }

    private void saveWordsOf(Study study, GPTResponse response) {
        List<Word> words = wordContentProcessor.parseGPTResponse(response, study.getId());
        wordRepository.saveAll(words);
    }

    private String generatePrompt(Long developerTypeId, List<String> excludeWords) {
        StringBuilder prompt = new StringBuilder();
        wordPromptManager.addPrompt(developerTypeId, prompt);
        wordPromptManager.addExcludePrompt(excludeWords, prompt);
        return prompt.toString();
    }
}
