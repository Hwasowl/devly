package se.sowl.devlybatch.job.userStudy;


import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlybatch.common.QuerydslPagingItemReader;
import se.sowl.devlybatch.config.StudyBatchProperties;
import se.sowl.devlybatch.job.userStudy.service.StudyAssignmentService;
import se.sowl.devlydomain.user.domain.UserStudy;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StudyAssignmentJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final StudyBatchProperties properties;
    private final StudyAssignmentService studyAssignmentService;

    @Bean
    public Job studyAssignmentJob() {
        return new JobBuilder("studyAssignmentJob", jobRepository)
            .start(assignStudiesStep())
            .build();
    }

    @Bean
    public Step assignStudiesStep() {
        return new StepBuilder("assignStudiesStep", jobRepository)
            .<UserStudy, UserStudy>chunk(properties.getChunkSize(), transactionManager)
            .reader(completedStudiesReader())
            .processor(nextStudyProcessor())
            .writer(newStudiesWriter())
            .faultTolerant()
            .retryLimit(properties.getRetryLimit())
            .retry(TransientDataAccessException.class)
            .skip(DuplicateKeyException.class)
            .skipLimit(properties.getSkipLimit())
            .listener(new StepExecutionListener() {
                @Override
                public void beforeStep(StepExecution stepExecution) {
                    log.info("Study assignment step started");
                }

                @Override
                public ExitStatus afterStep(StepExecution stepExecution) {
                    log.info("Study assignment step completed. Read count: {}", stepExecution.getReadCount());
                    return ExitStatus.COMPLETED;
                }
            })
            .build();
    }

    @Bean
    @StepScope
    protected ItemReader<UserStudy> completedStudiesReader() {
        return new QuerydslPagingItemReader<>(
            (pageable) -> {
                int pageNumber = pageable.getPageNumber();
                int pageSize = pageable.getPageSize();
                return studyAssignmentService.findCompletedStudiesWithoutNext(pageNumber, pageSize);
            },
            properties.getChunkSize()
        );
    }

    @Bean
    @StepScope
    protected ItemProcessor<UserStudy, UserStudy> nextStudyProcessor() {
        return studyAssignmentService::assignNextStudy;
    }

    @Bean
    @StepScope
    public JpaItemWriter<UserStudy> newStudiesWriter() {
        JpaItemWriter<UserStudy> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
