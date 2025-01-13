package se.sowl.devlybatch.job.study;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StudyCreationJobConfig {

    private final StudyRepository studyRepository;
    private final DeveloperTypeRepository developerTypeRepository;

    @Bean
    public Job studyCreationJob(JobRepository jobRepository, Step createStudiesStep) {
        return new JobBuilder("studyCreationJob", jobRepository)
            .start(createStudiesStep)
            .build();
    }

    @Bean
    public Step createStudiesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("createStudiesStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                List<Study> studies = getStudies();
                studyRepository.saveAll(studies);
                log.info("Created {} studies", studies.size());
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    private List<Study> getStudies() {
        List<DeveloperType> types = developerTypeRepository.findAll();
        return types.stream()
            .map(type -> Study.builder().typeId(1L).developerTypeId(type.getId()).build())
            .collect(Collectors.toList());
    }
}
