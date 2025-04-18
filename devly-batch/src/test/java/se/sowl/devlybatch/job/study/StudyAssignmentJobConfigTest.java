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
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserRepository;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.AssertionErrors.fail;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestBatchConfig.class, StudyAssignmentJobConfig.class, TestDataFactory.class})
class StudyAssignmentJobConfigTest extends MediumBatchTest {

    private static final int STUDIES_PER_TYPE = 20;
    private static final Long[] USER_IDS = {1L, 2L, 3L, 4L, 5L};
    private static final LocalDateTime YESTERDAY = LocalDateTime.now().minusDays(1)
        .withHour(12).withMinute(0).withSecond(0).withNano(0);

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserStudyRepository userStudyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Job studyAssignmentJob;

    @Autowired
    private TestDataFactory testDataFactory;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
        jobLauncherTestUtils.setJob(studyAssignmentJob);
        jobLauncherTestUtils.setJobRepository(jobRepository);
    }

    @AfterEach
    void cleanUp() {
        userStudyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        studyTypeRepository.deleteAllInBatch();
        developerTypeRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("완료된 스터디가 없으면 새로운 스터디가 할당되지 않는다")
    void noCompletedStudiesShouldNotAssignNewStudies() throws Exception {
        // given
        StudyType studyType = testDataFactory.createStudyType("StudyType", 100L);
        DeveloperType developerType = testDataFactory.createDeveloperType("Backend Developer");
        testDataFactory.createConnectedStudies(studyType, developerType, 2);

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(userStudyRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("완료된 스터디들에 대해 같은 타입의 다음 스터디가 할당된다")
    void completedStudiesShouldBeAssignedNextStudyOfSameType() throws Exception {
        // given
        Map<Long, List<Study>> studiesByType = testDataFactory.createStudiesForAllTypes(4, STUDIES_PER_TYPE);
        testDataFactory.setupMultipleUsersWithStudies(USER_IDS, studiesByType, YESTERDAY);

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        for (Long userId : USER_IDS) {
            List<UserStudy> userStudies = userStudyRepository.findAllWithStudyByUserId(userId);
            assertStudyAssignments(userStudies);
        }
    }

    @Test
    @DisplayName("할당 로직이 여러번 수행되도 조건이 맞지 않는다면 스터디가 추가로 할당되지 않아야 한다")
    void shouldBeNotAssignmentDuplicateUserStudy() throws Exception {
        // given
        Map<Long, List<Study>> studiesByType = testDataFactory.createStudiesForAllTypes(4, STUDIES_PER_TYPE);
        testDataFactory.setupMultipleUsersWithStudies(USER_IDS, studiesByType, YESTERDAY);

        // when
        JobExecution execution1 = jobLauncherTestUtils.launchJob();
        assertThat(execution1.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        JobExecution execution2 = jobLauncherTestUtils.launchJob();
        assertThat(execution2.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // then
        for (Long userId : USER_IDS) {
            List<UserStudy> userStudies = userStudyRepository.findAllWithStudyByUserId(userId);
            assertStudyAssignments(userStudies);
        }
    }

    private void assertStudyAssignments(List<UserStudy> userStudies) {
        System.out.println("Validating UserStudy entries - count: " + userStudies.size());
        userStudies.forEach(us -> System.out.println(
            String.format("UserStudy(id=%d, study.id=%d, study.type.id=%d, completed=%b)",
                us.getId(),
                us.getStudy().getId(),
                us.getStudy().getStudyType().getId(),
                us.isCompleted())
        ));

        Map<Long, List<UserStudy>> studiesByType = userStudies.stream()
            .collect(Collectors.groupingBy(us -> us.getStudy().getStudyType().getId()));

        System.out.println("Grouped by study type - types count: " + studiesByType.size());
        studiesByType.forEach((typeId, studies) ->
            System.out.println("Type " + typeId + ": " + studies.size() + " studies"));

        for (long typeId = 1; typeId <= 2; typeId++) {
            final Long finalTypeId = typeId;

            List<UserStudy> typeStudies = studiesByType.get(finalTypeId);
            if (typeStudies == null) {
                fail(String.format("타입 ID %d에 해당하는 UserStudy가 없습니다. 현재 그룹화된 타입 ID: %s",
                    finalTypeId, studiesByType.keySet()));
            }

            assertThat(typeStudies).hasSize(2)
                .withFailMessage("타입 %d의 스터디는 2개(완료된 스터디와 새 스터디)여야 합니다", typeId);

            UserStudy completedStudy = typeStudies.stream()
                .filter(UserStudy::isCompleted)
                .findFirst()
                .orElseThrow(() -> new AssertionError("완료된 스터디가 없습니다"));

            UserStudy newStudy = typeStudies.stream()
                .filter(us -> !us.isCompleted())
                .findFirst()
                .orElseThrow(() -> new AssertionError("새로 할당된 스터디가 없습니다"));

            assertThat(newStudy.getStudy().getId())
                .isGreaterThan(completedStudy.getStudy().getId());

            assertThat(newStudy.getStudy().getDeveloperType().getId())
                .isEqualTo(completedStudy.getStudy().getDeveloperType().getId());
        }

        for (long typeId = 3; typeId <= 4; typeId++) {
            final Long finalTypeId = typeId;

            List<UserStudy> typeStudies = studiesByType.get(finalTypeId);
            if (typeStudies == null) {
                fail(String.format("타입 ID %d에 해당하는 UserStudy가 없습니다. 현재 그룹화된 타입 ID: %s",
                    finalTypeId, studiesByType.keySet()));
            }

            assertThat(typeStudies).hasSize(1)
                .withFailMessage("미완료 타입 %d의 스터디는 1개만 있어야 합니다", typeId);

            assertThat(typeStudies.get(0).isCompleted()).isFalse();
        }
    }
}
