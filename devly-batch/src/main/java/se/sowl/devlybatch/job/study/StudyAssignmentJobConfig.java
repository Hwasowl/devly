package se.sowl.devlybatch.job.study;


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
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StudyAssignmentJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserStudyRepository userStudyRepository;
    private final StudyRepository studyRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final StudyBatchProperties properties;

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
    public ItemReader<UserStudy> completedStudiesReader() {
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDate today = LocalDate.now();
        return new QuerydslPagingItemReader<>(
            pageable -> userStudyRepository.findCompletedStudiesWithoutNext(yesterday, todayStart, today, pageable),
            properties.getChunkSize()
        );
    }

    @Bean
    @StepScope
    public ItemProcessor<UserStudy, UserStudy> nextStudyProcessor() {
        List<Study> orderedStudies = studyRepository.findAllByOrderById();
        Map<Long, Study> studyMap = orderedStudies.stream().collect(Collectors.toMap(Study::getId, s -> s));
        return completed -> {
            Study completedStudy = getCompletedStudy(completed, studyMap);
            if (completedStudy == null) return null;
            Study nextStudy = getNextStudy(completed, orderedStudies, completedStudy);
            if (nextStudy == null) return null;
            return UserStudy.builder()
                .userId(completed.getUserId())
                .studyId(nextStudy.getId())
                .scheduledAt(LocalDate.now())
                .build();
        };
    }

    private static Study getCompletedStudy(UserStudy completed, Map<Long, Study> studyMap) {
        Study completedStudy = studyMap.get(completed.getStudyId());
        if (completedStudy == null) {
            log.warn("Completed study not found: {}", completed.getStudyId());
            return null;
        }
        return completedStudy;
    }

    private static Study getNextStudy(UserStudy completed, List<Study> orderedStudies, Study completedStudy) {
        Study nextStudy = findNextStudy(orderedStudies, completedStudy);
        if (nextStudy == null) {
            log.info("No next study found for user: {}, type: {}, devType: {}",
                completed.getUserId(), completedStudy.getTypeId(), completedStudy.getDeveloperTypeId());
            return null;
        }
        return nextStudy;
    }

    public static Study findNextStudy(List<Study> allStudies, Study currentStudy) {
        boolean foundCurrent = false;
        for (Study study : allStudies) {
            if (foundCurrent && isSameStudyType(currentStudy, study)) return study;
            else if (study.getId().equals(currentStudy.getId())) foundCurrent = true;
        }
        return null;
    }

    private static boolean isSameStudyType(Study currentStudy, Study study) {
        return study.getTypeId().equals(currentStudy.getTypeId()) &&
            study.getDeveloperTypeId().equals(currentStudy.getDeveloperTypeId());
    }

    @Bean
    @StepScope
    public JpaItemWriter<UserStudy> newStudiesWriter() {
        JpaItemWriter<UserStudy> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
