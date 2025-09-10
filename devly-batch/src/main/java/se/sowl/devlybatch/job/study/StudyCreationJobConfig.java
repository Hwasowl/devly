package se.sowl.devlybatch.job.study;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlybatch.job.common.BaseStudyCreationJobConfig;
import se.sowl.devlybatch.job.study.exception.CreateStudyFailedException;
import se.sowl.devlybatch.job.study.service.StudyService;
import se.sowl.devlydomain.study.domain.Study;

import java.util.List;

@Slf4j
@Configuration
public class StudyCreationJobConfig extends BaseStudyCreationJobConfig {

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
    protected Object executeStudyCreation() {
        return studyService.generateStudiesOf();
    }

    @Override
    protected void logCreationResult(Object result) {
        List<Study> studies = (List<Study>) result;
        log.info("스터디 {}개 생성 완료", studies.size());
    }

    @Override
    protected RuntimeException createJobException(String message, Throwable cause) {
        return new CreateStudyFailedException(message, cause);
    }

    @Override
    protected String getJobName() {
        return "studyCreationJob";
    }

    @Override
    protected String getStepName() {
        return "createStudiesStep";
    }
}
