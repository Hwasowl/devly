package se.sowl.devlybatch.job.study;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import se.sowl.devlybatch.config.TestBatchConfig;
import se.sowl.devlybatch.job.MediumBatchTest;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.study.domain.Study;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestBatchConfig.class, StudyCreationJobConfig.class})
class StudyCreationJobConfigTest extends MediumBatchTest {

    @Autowired
    private Job studyCreationJob;

    @BeforeEach
    void setUp() {
        initializeJobLauncherTestUtils(studyCreationJob);
        studyTypeRepository.saveAll(createStandardStudyTypes());
    }

    @AfterEach
    void tearDown() {
        studyRepository.deleteAll();
        developerTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("createStudiesStep은 각 개발자 타입별로 스터디를 생성한다")
    void shouldCreateStudiesForEachDeveloperType() throws Exception {
        // given
        List<DeveloperType> developerTypes = developerTypeRepository.saveAll(createStandardDeveloperTypes());

        DeveloperType backendDeveloper = developerTypes.stream()
            .filter(developerType -> developerType.getName().equals("Backend Developer"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Backend Developer type not found"));

        DeveloperType frontendDeveloper = developerTypes.stream()
            .filter(developerType -> developerType.getName().equals("Frontend Developer"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Frontend Developer type not found"));

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createStudiesStep");

        // then
        assertJobExecutionCompleted(jobExecution);

        List<Study> allStudies = studyRepository.findAll();
        assertThat(allStudies).hasSize(4);

        List<Study> backendStudies = studyRepository.findByDeveloperTypeId(backendDeveloper.getId());
        assertThat(backendStudies).hasSize(2);

        List<Study> frontendStudies = studyRepository.findByDeveloperTypeId(frontendDeveloper.getId());
        assertThat(frontendStudies).hasSize(2);
    }
}
