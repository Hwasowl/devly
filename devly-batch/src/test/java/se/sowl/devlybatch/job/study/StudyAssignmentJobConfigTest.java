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
import org.springframework.test.util.ReflectionTestUtils;
import se.sowl.devlybatch.config.TestBatchConfig;
import se.sowl.devlybatch.job.MediumBatchTest;
import se.sowl.devlybatch.job.userStudy.StudyAssignmentJobConfig;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestBatchConfig.class, StudyAssignmentJobConfig.class})
class StudyAssignmentJobConfigTest extends MediumBatchTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserStudyRepository userStudyRepository;

    @Autowired
    private Job studyAssignmentJob;

    private static final int STUDIES_PER_TYPE = 20;
    private static final Long[] USER_IDS = {1L, 2L, 3L, 4L, 5L};
    private static final LocalDateTime YESTERDAY = LocalDateTime.now().minusDays(1)
        .withHour(12).withMinute(0).withSecond(0).withNano(0);

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
        jobLauncherTestUtils.setJob(studyAssignmentJob);
        jobLauncherTestUtils.setJobRepository(jobRepository);
    }

    @AfterEach
    void cleanUp() {
        userStudyRepository.deleteAll();
        studyRepository.deleteAll();
    }

    @Test
    @DisplayName("완료된 스터디가 없으면 새로운 스터디가 할당되지 않는다")
    void noCompletedStudiesShouldNotAssignNewStudies() throws Exception {
        createConnectedStudies(1L, 1L, 2);

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(userStudyRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("완료된 스터디들에 대해 같은 타입의 다음 스터디가 할당된다")
    void completedStudiesShouldBeAssignedNextStudyOfSameType() throws Exception {
        Map<Long, List<Study>> studiesByType = createStudiesForAllTypes();
        setupUserStudies(studiesByType);

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        for (Long userId : USER_IDS) {
            List<UserStudy> userStudies = userStudyRepository.findAllWithStudyByUserId(userId);
            assertStudyAssignments(userStudies);
        }
    }

    private Map<Long, List<Study>> createStudiesForAllTypes() {
        Map<Long, List<Study>> studiesByType = new HashMap<>();

        for (long typeId = 0; typeId < 4; typeId++) {
            List<Study> typeStudies = createConnectedStudies(typeId, 1L, STUDIES_PER_TYPE);
            studiesByType.put(typeId, typeStudies);
        }

        return studiesByType;
    }

    private void setupUserStudies(Map<Long, List<Study>> studiesByType) {
        for (Long userId : USER_IDS) {
            for (long typeId = 0; typeId <= 1; typeId++) {
                Study study = studiesByType.get(typeId).get(0);
                userStudyRepository.save(createUserStudy(userId, study, true, YESTERDAY));
            }

            for (long typeId = 2; typeId <= 3; typeId++) {
                Study study = studiesByType.get(typeId).get(0);
                userStudyRepository.save(createUserStudy(userId, study, false, null));
            }
        }
    }

    private List<Study> createConnectedStudies(Long typeId, Long devTypeId, int count) {
        List<Study> studies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            studies.add(Study.builder()
                .typeId(typeId)
                .developerTypeId(devTypeId)
                .build());
        }
        studies.forEach(Study::connect);
        return studyRepository.saveAll(studies);
    }

    private UserStudy createUserStudy(Long userId, Study study, boolean completed, LocalDateTime completedAt) {
        UserStudy userStudy = UserStudy.builder()
            .userId(userId)
            .study(study)
            .scheduledAt(LocalDateTime.now().minusDays(1))
            .build();

        if (completed) {
            userStudy.complete();
            ReflectionTestUtils.setField(userStudy, "completedAt", completedAt);
        }

        return userStudy;
    }

    private void assertStudyAssignments(List<UserStudy> userStudies) {
        Map<Long, List<UserStudy>> studiesByType = userStudies.stream()
            .collect(Collectors.groupingBy(us -> us.getStudy().getTypeId()));

        for (long typeId = 0; typeId <= 1; typeId++) {
            List<UserStudy> typeStudies = studiesByType.get(typeId);
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

            assertThat(newStudy.getStudy().getTypeId())
                .isEqualTo(completedStudy.getStudy().getTypeId());
        }

        for (long typeId = 2; typeId <= 3; typeId++) {
            List<UserStudy> typeStudies = studiesByType.get(typeId);
            assertThat(typeStudies).hasSize(1)
                .withFailMessage("미완료 타입 %d의 스터디는 1개만 있어야 합니다", typeId);

            assertThat(typeStudies.get(0).isCompleted()).isFalse();
        }
    }
}
