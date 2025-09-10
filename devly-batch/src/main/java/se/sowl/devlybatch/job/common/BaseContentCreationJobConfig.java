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
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatus;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public abstract class BaseContentCreationJobConfig {
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
                    processTodayContent();
                    return RepeatStatus.FINISHED;
                } catch (Exception e) {
                    log.error("{} 콘텐츠 생성 처리 실패: {}", getContentTypeName(), e.getMessage(), e);
                    throw e;
                }
            }, transactionManager)
            .build();
    }
    
    protected void processTodayContent() {
        List<Study> todayStudies = studyService.getTodayStudiesOf(getStudyTypeId(), StudyStatus.UNCONNECTED);
        for (Study study : todayStudies) {
            try {
                processStudyContent(study);
                log.info("스터디 {}에 {} 생성 완료", study.getId(), getContentTypeName());
            } catch (Exception e) {
                log.error("{} 생성 실패 - 스터디 ID: {}, 오류: {}",
                    getContentTypeName(), study.getId(), e.getMessage(), e);
            }
        }
    }
    
    protected abstract void processStudyContent(Study study);
    protected abstract Long getStudyTypeId();
    protected abstract String getJobName();
    protected abstract String getStepName();
    protected abstract String getContentTypeName();
}