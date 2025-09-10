package se.sowl.devlybatch.job.word;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlybatch.job.common.BaseStudyJobConfig;
import se.sowl.devlybatch.job.study.service.StudyService;
import se.sowl.devlybatch.job.word.service.WordProcessService;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatus;
import se.sowl.devlydomain.study.domain.StudyTypeClassification;

import java.util.List;

@Slf4j
@Configuration
public class WordCreationJobConfig extends BaseStudyJobConfig {
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
    protected void processTodayStudies() {
        List<Study> todayStudies = studyService.getTodayStudiesOf(StudyTypeClassification.WORD.getId(), StudyStatus.UNCONNECTED);
        log.info("Create word batch started! studies total count: {}", todayStudies.size());
        
        for (Study study : todayStudies) {
            try {
                Long studyId = wordProcessService.progressWordsOfStudy(study);
                log.info("Successfully created words for study {}", studyId);
            } catch (Exception e) {
                log.error("Failed to create words for study {}: {}", study.getId(), e.getMessage(), e);
            }
        }
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
    protected String getStudyTypeName() {
        return "Word";
    }
}
