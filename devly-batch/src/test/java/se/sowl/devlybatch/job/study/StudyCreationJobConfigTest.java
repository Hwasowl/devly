package se.sowl.devlybatch.job.study;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import se.sowl.devlybatch.config.TestBatchConfig;
import se.sowl.devlybatch.job.MediumBatchTest;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestBatchConfig.class, StudyCreationJobConfig.class})
class StudyCreationJobConfigTest extends MediumBatchTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private DeveloperTypeRepository developerTypeRepository;

    @Autowired
    private Job studyCreationJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
        jobLauncherTestUtils.setJob(studyCreationJob);
        jobLauncherTestUtils.setJobRepository(jobRepository);
    }

    @AfterEach
    void tearDown() {
        studyRepository.deleteAll();
        developerTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("createStudiesStep은 각 개발자 타입별로 스터디를 생성한다")
    void createStudiesStepTest() throws Exception {
        // given
        List<DeveloperType> developerTypes = developerTypeRepository.saveAll(getDeveloperTypes());
        DeveloperType backend = developerTypes.stream().filter(developerType -> developerType.getName().equals("Backend Developer")).findFirst().orElseThrow();
        DeveloperType frontend = developerTypes.stream().filter(developerType -> developerType.getName().equals("Frontend Developer")).findFirst().orElseThrow();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createStudiesStep");

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<Study> savedStudies = studyRepository.findAll();
        assertThat(savedStudies).hasSize(2);

        Study backendStudy = studyRepository.findByDeveloperTypeId(backend.getId());
        assertThat(backendStudy.getTypeId()).isEqualTo(1L);
        assertThat(backendStudy.getDeveloperTypeId()).isEqualTo(backend.getId());

        Study frontendStudy = studyRepository.findByDeveloperTypeId(frontend.getId());
        assertThat(frontendStudy.getTypeId()).isEqualTo(1L);
        assertThat(frontendStudy.getDeveloperTypeId()).isEqualTo(frontend.getId());
    }

    @Test
    @DisplayName("개발자 타입이 없으면 스터디가 생성되지 않는다")
    void createStudiesStep_WithNoDeveloperTypes_CreatesNoStudies() throws Exception {
        // given
        // 개발자 타입 데이터를 추가하지 않음

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createStudiesStep");

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<Study> savedStudies = studyRepository.findAll();
        assertThat(savedStudies).isEmpty();
    }
}
