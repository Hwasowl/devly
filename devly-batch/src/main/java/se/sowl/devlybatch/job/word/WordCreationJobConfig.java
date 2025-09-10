package se.sowl.devlybatch.job.word;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlybatch.job.common.BaseContentCreationJobConfig;
import se.sowl.devlybatch.job.study.service.StudyService;
import se.sowl.devlybatch.job.word.service.WordProcessService;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyTypeClassification;

@Slf4j
@Configuration
public class WordCreationJobConfig extends BaseContentCreationJobConfig {
    private final WordProcessService wordProcessService;

    public WordCreationJobConfig(StudyService studyService, WordProcessService wordProcessService) {
        super(studyService);
        this.wordProcessService = wordProcessService;
    }

    @Bean
    public Job wordCreationJob(JobRepository jobRepository, Step createWordsStep) {
        return createJob(jobRepository, createWordsStep);
    }

    @Bean
    public Step createWordsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return createStep(jobRepository, transactionManager);
    }

    @Override
    protected void processStudyContent(Study study) {
        wordProcessService.progressWordsOfStudy(study);
    }

    @Override
    protected Long getStudyTypeId() {
        return StudyTypeClassification.WORD.getId();
    }

    @Override
    protected String getJobName() {
        return "wordCreationJob";
    }

    @Override
    protected String getStepName() {
        return "createWordsStep";
    }

    @Override
    protected String getContentTypeName() {
        return "Word";
    }
}
