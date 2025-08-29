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
import se.sowl.devlybatch.job.study.service.StudyService;
import se.sowl.devlybatch.job.word.service.WordProcessService;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatus;
import se.sowl.devlydomain.study.domain.StudyTypeClassification;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WordCreationJobConfig {
    private final StudyService studyService;
    private final WordProcessService wordProcessService;

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

    public void createTodayWordStudies() {
        List<Study> todayStudies = studyService.getTodayStudiesOf(StudyTypeClassification.WORD.getId(), StudyStatus.UNCONNECTED);
        for (Study study : todayStudies) {
            try {
                Long studyId = wordProcessService.progressWordsOfStudy(study);
                log.info("Successfully created words for study {}", studyId);
            } catch (Exception e) {
                log.error("Failed to create words for study {}: {}", study.getId(), e.getMessage(), e);
            }
        }
    }
}
