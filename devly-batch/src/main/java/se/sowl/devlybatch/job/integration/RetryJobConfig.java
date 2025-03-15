package se.sowl.devlybatch.job.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RetryJobConfig {
    private final JobRepository jobRepository;
    private final JobLauncher jobLauncher;
    private final Step createWordsStep;
    private final Step createPrStep;
    private final Step assignStudiesStep;

    private final long EVERY_3_HOURS = 3 * 60 * 60 * 1000;

    @Bean
    public Job retryStudyJob() {
        return new JobBuilder("retryStudyJob", jobRepository)
            .start(createWordsStep)
            .next(createPrStep)
            .next(assignStudiesStep)
            .build();
    }

    @Scheduled(fixedRate = EVERY_3_HOURS)
    public void runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            jobLauncher.run(retryStudyJob(), jobParameters);
            log.info("Retry study job completed successfully");
        } catch (Exception e) {
            log.error("Failed to run daily study job", e);
        }
    }
}
