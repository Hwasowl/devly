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
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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
    void noStudies() throws Exception {
        // given
        createStudies();

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(userStudyRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("완료된 스터디들에 대해 같은 타입의 다음 스터디가 할당된다")
    void assignmentLargeScaleStudies() throws Exception {
        // given
        List<Study> allStudies = new ArrayList<>();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0);

        for (int typeId = 0; typeId < 4; typeId++) {
            allStudies.addAll(createOrderedStudies((long) typeId, 1L, STUDIES_PER_TYPE));
        }

        studyRepository.saveAll(allStudies);

        for (Long userId : USER_IDS) {
            saveCompletedStudies(userId, allStudies, yesterday);
            saveInCompletedStudies(userId, allStudies);
        }

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        for (Long userId : USER_IDS) {
            List<UserStudy> userStudies = userStudyRepository.findAllWithStudyByUserId(userId);
            assertStudyAssignments(userStudies);
        }
    }

    private void saveInCompletedStudies(Long userId, List<Study> allStudies) {
        Study study3 = findFirstStudyByType(allStudies, 2L);
        Study study4 = findFirstStudyByType(allStudies, 3L);
        UserStudy incomplete1 = createUserStudy(userId, study3, false, null);
        UserStudy incomplete2 = createUserStudy(userId, study4, false, null);
        userStudyRepository.saveAll(Arrays.asList(incomplete1, incomplete2));
    }

    private void saveCompletedStudies(Long userId, List<Study> allStudies, LocalDateTime yesterday) {
        Study study1 = findFirstStudyByType(allStudies, 0L);
        Study study2 = findFirstStudyByType(allStudies, 1L);
        UserStudy completed1 = createUserStudy(userId, study1, true, yesterday);
        UserStudy completed2 = createUserStudy(userId, study2, true, yesterday);
        userStudyRepository.saveAll(Arrays.asList(completed1, completed2));
    }

    protected void assertStudyAssignments(List<UserStudy> userStudies) {
        Map<Long, List<UserStudy>> studiesByType = groupStudiesByType(userStudies);
        // 완료된 타입에 대한 검증
        Arrays.asList(0L, 1L).forEach(typeId -> assertCompletedTypeStudies(studiesByType.get(typeId)));
        // 미완료된 타입에 대한 검증
        Arrays.asList(2L, 3L).forEach(typeId -> assertIncompleteTypeStudies(studiesByType.get(typeId)));
    }

    private Map<Long, List<UserStudy>> groupStudiesByType(List<UserStudy> userStudies) {
        return userStudies.stream().collect(Collectors.groupingBy(us -> us.getStudy().getTypeId()));
    }

    private void assertCompletedTypeStudies(List<UserStudy> typeStudies) {
        assertThat(typeStudies).hasSize(2);

        UserStudy completedStudy = findCompletedStudy(typeStudies);
        UserStudy newStudy = findIncompleteStudy(typeStudies);

        assertNewStudyIsValid(completedStudy, newStudy);
    }

    private void assertIncompleteTypeStudies(List<UserStudy> typeStudies) {
        assertThat(typeStudies).hasSize(1);
        assertThat(typeStudies.get(0).isCompleted()).isFalse();
    }

    private UserStudy findCompletedStudy(List<UserStudy> studies) {
        return studies.stream()
            .filter(UserStudy::isCompleted)
            .findFirst()
            .orElseThrow(AssertionError::new);
    }

    private UserStudy findIncompleteStudy(List<UserStudy> studies) {
        return studies.stream()
            .filter(us -> !us.isCompleted())
            .findFirst()
            .orElseThrow(AssertionError::new);
    }

    private void assertNewStudyIsValid(UserStudy completedStudy, UserStudy newStudy) {
        // 새로운 스터디의 ID가 완료된 스터디의 ID보다 커야 함 (도메인 규칙)
        assertThat(newStudy.getStudy().getId())
            .isGreaterThan(completedStudy.getStudy().getId());

        Study completedStudyEntity = completedStudy.getStudy();
        Study newStudyEntity = newStudy.getStudy();
        assertThat(newStudyEntity.getTypeId())
            .isEqualTo(completedStudyEntity.getTypeId());
    }

    private List<Study> createOrderedStudies(Long typeId, Long devTypeId, int count) {
        List<Study> studies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            studies.add(Study.builder()
                .typeId(typeId)
                .developerTypeId(devTypeId)
                .build());
        }
        return studyRepository.saveAll(studies);
    }

    private void createStudies(Long typeId, Long devTypeId, int count) {
        List<Study> studies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            studies.add(Study.builder()
                .typeId(typeId)
                .developerTypeId(devTypeId)
                .build()
            );
        }
        studyRepository.saveAll(studies);
    }

    private void createStudies() {
        createStudies(1L, 1L, 2);
    }

    private UserStudy createUserStudy(Long userId, Study study, boolean completed, LocalDateTime completedAt) {
        UserStudy userStudy = UserStudy.builder()
            .userId(userId)
            .study(study)
            .scheduledAt(LocalDateTime.now().minusDays(1))  // 어제 날짜로 설정
            .build();

        if (completed) {
            userStudy.complete();
            ReflectionTestUtils.setField(userStudy, "completedAt", completedAt);
        }

        return userStudy;
    }

    private Study findFirstStudyByType(List<Study> studies, Long typeId) {
        return studies.stream()
            .filter(s -> s.getTypeId().equals(typeId))
            .min(Comparator.comparing(Study::getId))
            .orElseThrow(() -> new IllegalStateException("Study not found for type: " + typeId));
    }
}


