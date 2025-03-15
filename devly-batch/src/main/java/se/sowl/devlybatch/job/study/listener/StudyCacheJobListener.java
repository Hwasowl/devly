package se.sowl.devlybatch.job.study.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;
import se.sowl.devlybatch.job.study.cache.StudyCache;

@Component
@Slf4j
@RequiredArgsConstructor
public class StudyCacheJobListener implements JobExecutionListener {
    private final StudyCache studyCache;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        studyCache.clearCache();
        log.info("Study cache cleared before job: {}", jobExecution.getJobInstance().getJobName());
    }
}
