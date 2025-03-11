package se.sowl.devlybatch.job.study;


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
import se.sowl.devlydomain.study.domain.Study;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StudyCreationJobConfig {
    private final StudyService studyService;

    @Bean
    public Job studyCreationJob(JobRepository jobRepository, Step createStudiesStep) {
        return new JobBuilder("studyCreationJob", jobRepository)
            .start(createStudiesStep)
            .build();
    }

    @Bean
    public Step createStudiesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("createStudiesStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                List<Study> studies = studyService.generateStudiesOf();
                log.info("Created {} studies", studies.size());
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
