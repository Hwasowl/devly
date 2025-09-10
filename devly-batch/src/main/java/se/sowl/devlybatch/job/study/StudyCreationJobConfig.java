package se.sowl.devlybatch.job.study;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlybatch.job.common.BaseStudyJobConfig;
import se.sowl.devlybatch.job.study.exception.CreateStudyFailedException;
import se.sowl.devlybatch.job.study.service.StudyService;
import se.sowl.devlydomain.study.domain.Study;

import java.util.List;

@Slf4j
@Configuration
public class StudyCreationJobConfig extends BaseStudyJobConfig {

    public StudyCreationJobConfig(StudyService studyService) {
        super(studyService);
    }

    @Bean
    public Job studyCreationJob(JobRepository jobRepository, Step createStudiesStep) {
        return createJob(jobRepository, createStudiesStep);
    }

    @Bean
    public Step createStudiesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return createStep(jobRepository, transactionManager);
    }

    @Override
    protected void processTodayStudies() {
        try {
            List<Study> studies = studyService.generateStudiesOf();
            log.info("Created {} studies", studies.size());
        } catch (Exception error) {
            log.error("Failed to create studies!", error);
            throw new CreateStudyFailedException("Failed to create studies", error);
        }
    }

    @Override
    protected String getJobName() {
        return "studyCreationJob";
    }

    @Override
    protected String getStepName() {
        return "createStudiesStep";
    }

    @Override
    protected String getStudyTypeName() {
        return "Study";
    }
}
