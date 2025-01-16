package se.sowl.devlybatch.config;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@Import(BatchAutoConfiguration.class)
@EnableJpaAuditing
public class TestBatchConfig {
    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils(Job wordCreationJob, JobLauncher jobLauncher, JobRepository jobRepository) {
        JobLauncherTestUtils utils = new JobLauncherTestUtils();
        utils.setJob(wordCreationJob);
        utils.setJobLauncher(jobLauncher);
        utils.setJobRepository(jobRepository);
        return utils;
    }

    @Bean
    public JobRepositoryTestUtils jobRepositoryTestUtils(JobRepository jobRepository) {
        return new JobRepositoryTestUtils(jobRepository);
    }
}
