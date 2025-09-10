package se.sowl.devlybatch.job.pr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlybatch.job.common.BaseContentCreationJobConfig;
import se.sowl.devlybatch.job.pr.service.PrProcessService;
import se.sowl.devlybatch.job.study.service.StudyService;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyTypeClassification;

@Slf4j
@Configuration
public class PrCreationJobConfig extends BaseContentCreationJobConfig {
    private final PrProcessService prProcessService;

    public PrCreationJobConfig(StudyService studyService, PrProcessService prProcessService) {
        super(studyService);
        this.prProcessService = prProcessService;
    }

    @Bean
    public Job prCreationJob(JobRepository jobRepository, Step createPrStep) {
        return createJob(jobRepository, createPrStep);
    }

    @Bean
    public Step createPrStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return createStep(jobRepository, transactionManager);
    }

    @Override
    protected void processStudyContent(Study study) {
        prProcessService.processPrStudies(study);
    }

    @Override
    protected Long getStudyTypeId() {
        return StudyTypeClassification.PULL_REQUEST.getId();
    }

    @Override
    protected String getJobName() {
        return "createPrJob";
    }

    @Override
    protected String getStepName() {
        return "createPrStep";
    }

    @Override
    protected String getContentTypeName() {
        return "PR";
    }
}

