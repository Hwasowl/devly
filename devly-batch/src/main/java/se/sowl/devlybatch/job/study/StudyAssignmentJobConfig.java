package se.sowl.devlybatch.job.study;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlybatch.config.BatchProperties;
import se.sowl.devlybatch.job.study.listener.StudyAssignmentListener;
import se.sowl.devlybatch.job.study.service.CompletedStudiesReader;
import se.sowl.devlybatch.job.study.service.StudyAssignmentService;
import se.sowl.devlybatch.job.study.service.StudyService;
import se.sowl.devlybatch.job.study.service.UserStudyBatchWriter;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StudyAssignmentJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchProperties properties;
    private final StudyAssignmentService studyAssignmentService;
    private final StudyService studyService;
    private final UserStudyRepository userStudyRepository;
    private final StudyAssignmentListener studyAssignmentListener;

    @Bean
    public Job studyAssignmentJob() {
        return new JobBuilder("studyAssignmentJob", jobRepository)
            .start(assignStudiesStep())
            .build();
    }

    @Bean
    public Step assignStudiesStep() {
        return new StepBuilder("assignStudiesStep", jobRepository)
            .<List<UserStudy>, List<UserStudy>>chunk(1, transactionManager)
            .reader(completedStudiesPageReader())
            .processor(nextStudiesPageProcessor())
            .writer(newStudiesWriter())
            .faultTolerant()
            .retryLimit(properties.getRetryLimit())
            .retry(TransientDataAccessException.class)
            .skip(DuplicateKeyException.class)
            .skipLimit(properties.getSkipLimit())
            .listener(studyAssignmentListener)
            .build();
    }

    @Bean
    @StepScope
    protected ItemReader<List<UserStudy>> completedStudiesPageReader() {
        return new CompletedStudiesReader(studyService, properties.getChunkSize());
    }

    @Bean
    @StepScope
    protected ItemProcessor<List<UserStudy>, List<UserStudy>> nextStudiesPageProcessor() {
        return studyAssignmentService::assignNextStudiesForPage;
    }

    @Bean
    @StepScope
    public ItemWriter<List<UserStudy>> newStudiesWriter() {
        return new UserStudyBatchWriter(userStudyRepository);
    }
}
