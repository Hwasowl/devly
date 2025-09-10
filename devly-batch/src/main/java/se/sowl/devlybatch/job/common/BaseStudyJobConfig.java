package se.sowl.devlybatch.job.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlybatch.job.study.service.StudyService;

@Slf4j
@Configuration
@RequiredArgsConstructor
public abstract class BaseStudyJobConfig {
    protected final StudyService studyService;

    protected Job createJob(JobRepository jobRepository, Step step) {
        return new JobBuilder(getJobName(), jobRepository)
            .start(step)
            .build();
    }

    protected Step createStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(getStepName(), jobRepository)
            .tasklet((contribution, chunkContext) -> {
                try {
                    processTodayStudies();
                    return RepeatStatus.FINISHED;
                } catch (Exception e) {
                    log.error("Failed to process {} studies: {}", getStudyTypeName(), e.getMessage(), e);
                    throw e;
                }
            }, transactionManager)
            .build();
    }

    protected abstract void processTodayStudies();

    protected abstract String getJobName();

    protected abstract String getStepName();

    protected abstract String getStudyTypeName();
}