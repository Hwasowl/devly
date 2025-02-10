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
public class DailyStudyJobConfig {

    private final JobRepository jobRepository;
    private final JobLauncher jobLauncher;
    private final Step createStudiesStep;
    private final Step createWordsStep;
    private final Step assignStudiesStep;

    @Bean
    public Job dailyStudyJob() {
        return new JobBuilder("dailyStudyJob", jobRepository)
            .start(createStudiesStep)
            .on("COMPLETED").to(createWordsStep)
            .from(createWordsStep).on("COMPLETED").end()
            .from(assignStudiesStep).on("*").fail()
            .from(assignStudiesStep).on("COMPLETED").end()
            .from(assignStudiesStep).on("*").fail()
            .end()
            .build();
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void runJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

            jobLauncher.run(dailyStudyJob(), jobParameters);
            log.info("Daily study job completed successfully");
        } catch (Exception e) {
            log.error("Failed to run daily study job", e);
        }
    }
}
