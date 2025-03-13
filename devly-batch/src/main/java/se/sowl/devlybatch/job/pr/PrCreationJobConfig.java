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
import se.sowl.devlybatch.job.pr.service.PrProcessService;
import se.sowl.devlybatch.job.study.service.StudyService;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PrCreationJobConfig {
    private final StudyService studyService;
    private final PrProcessService prProcessService;

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

    public void createTodayPrStudies() {
        List<Study> todayStudies = studyService.getTodayStudiesOf(StudyTypeEnum.PULL_REQUEST.getId());
        log.info("Create pr batch started! studies total count: {}", todayStudies.size());
        for (Study study : todayStudies) {
            try {
                prProcessService.processPrStudies(study);
                log.info("Created pr for study {}", study.getId());
            } catch (Exception e) {
                log.error("[Warning] pr create failed : Study ID={}, Error={}", study.getId(), e.getMessage(), e);
            }
        }
    }

}

