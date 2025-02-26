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
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StudyCreationJobConfig {

    private final StudyRepository studyRepository;
    private final StudyTypeRepository studyTypeRepository;
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
                List<DeveloperType> devTypes = developerTypeRepository.findAll();
                List<Study> studies = generateStudiesOf(devTypes);
                studyRepository.saveAll(studies);
                log.info("Created {} studies", studies.size());
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    private List<Study> generateStudiesOf(List<DeveloperType> devTypes) {
        // TODO: 스터디 배치 잡이 모두 구현 완료된다면 수정해야 한다. 현재 일부만 구현되었으므로 하드코딩으로 구현
        List<StudyType> studyTypes = studyTypeRepository.findAll();
        StudyType wordType = studyTypes.stream().filter(studyType -> studyType.getName().equals("word")).findFirst().get();
        StudyType prType = studyTypes.stream().filter(studyType -> studyType.getName().equals("pr")).findFirst().get();
        List<Study> types = new ArrayList<>(List.of());
        for(DeveloperType devType : devTypes) {
            Study study = Study.builder().typeId(wordType.getId()).developerTypeId(devType.getId()).build();
            Study prStudy = Study.builder().typeId(prType.getId()).developerTypeId(devType.getId()).build();
            types.add(study);
            types.add(prStudy);
        }
        return types;
    }
}
