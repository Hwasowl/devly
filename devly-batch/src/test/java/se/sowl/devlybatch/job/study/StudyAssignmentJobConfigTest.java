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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import se.sowl.devlybatch.config.TestBatchConfig;
import se.sowl.devlybatch.job.MediumBatchTest;
import se.sowl.devlybatch.job.study.cache.StudyCache;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserRepository;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class StudyAssignmentJobConfigTest extends MediumBatchTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserStudyRepository userStudyRepository;

    @Autowired
    private Job studyAssignmentJob;

    @Autowired
    private StudyCache studyCache;

    private static final int STUDIES_PER_TYPE = 20;
    private static final Long[] USER_IDS = {1L, 2L, 3L, 4L, 5L};
    private static final LocalDateTime YESTERDAY = LocalDateTime.now().minusDays(1)
        .withHour(12).withMinute(0).withSecond(0).withNano(0);
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
        jobLauncherTestUtils.setJob(studyAssignmentJob);
        jobLauncherTestUtils.setJobRepository(jobRepository);
        studyCache.clearCache();
        studyTypeRepository.saveAll(List.of(
            StudyType.builder().name("Word").build(),
            StudyType.builder().name("Knowledge").build(),
            StudyType.builder().name("PR").build(),
            StudyType.builder().name("Discussion").build()
        ));
        developerTypeRepository.saveAll(List.of(
            DeveloperType.builder().name("Backend Developer").build(),
            DeveloperType.builder().name("Frontend Developer").build()
        ));
    }
    @AfterEach
    void cleanUp() {
        userStudyRepository.deleteAll();
        studyRepository.deleteAll();
        userRepository.deleteAll();
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

    @Test
    @DisplayName("할당 로직이 여러번 수행되도 조건이 맞지 않는다면 스터디가 추가로 할당되지 않아야 한다.")
    void shouldBeNotAssignmentDuplicateUserStudy() throws Exception {
        Map<Long, List<Study>> studiesByType = createStudiesForAllTypes();
        setupUserStudies(studiesByType);

        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        JobExecution execution2 = jobLauncherTestUtils.launchJob();
        assertThat(execution2.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        for (Long userId : USER_IDS) {
            List<UserStudy> userStudies = userStudyRepository.findAllWithStudyByUserId(userId);
            assertStudyAssignments(userStudies);
        }
    }

    private Map<Long, List<Study>> createStudiesForAllTypes() {
        Map<Long, List<Study>> studiesByType = new HashMap<>();

        for (long typeId = 1; typeId <= 4; typeId++) {
            List<Study> typeStudies = createConnectedStudies(typeId, 1L, STUDIES_PER_TYPE);
            studiesByType.put(typeId, typeStudies);
        }

        return studiesByType;
    }

    private void setupUserStudies(Map<Long, List<Study>> studiesByType) {
        DeveloperType developerType = developerTypeRepository.findAll().stream()
            .filter(d -> d.getId().equals(1L))
            .findFirst()
            .orElseThrow();

        // 먼저 모든 사용자를 한 번씩만 생성
        Map<Long, User> userMap = new HashMap<>();
        for (Long userId : USER_IDS) {
            User user = userRepository.save(createUser(
                userId,
                developerType,
                "User" + userId,
                "nickname" + userId,
                userId + "@naver.com",
                "google"
            ));
            userMap.put(userId, user);
        }

        // 생성된 사용자를 재사용하여 UserStudy 생성
        for (Long userId : USER_IDS) {
            User user = userMap.get(userId);

            for (long typeId = 1; typeId <= 2; typeId++) {
                Study study = studiesByType.get(typeId).get(0);
                userStudyRepository.save(createUserStudy(user, study, true, YESTERDAY));
            }

            for (long typeId = 3; typeId <= 4; typeId++) {
                Study study = studiesByType.get(typeId).get(0);
                userStudyRepository.save(createUserStudy(user, study, false, null));
            }
        }
    }

    private List<Study> createConnectedStudies(Long typeId, Long devTypeId, int count) {
        StudyType studyType = studyTypeRepository.findAll().stream()
            .filter(s -> s.getId().equals(typeId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No study type found for ID " + typeId));
        DeveloperType developerType = developerTypeRepository.findAll().stream().filter(d -> d.getId().equals(devTypeId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No developer type found for ID " + devTypeId));
        List<Study> studies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            studies.add(buildStudy(studyType, developerType));
        }
        studies.forEach(Study::connect);
        return studyRepository.saveAll(studies);
    }

    private UserStudy createUserStudy(User user, Study study, boolean completed, LocalDateTime completedAt) {
        UserStudy userStudy = UserStudy.builder()
            .user(user)
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
            .collect(Collectors.groupingBy(us -> us.getStudy().getStudyType().getId()));

        for (long typeId = 1; typeId <= 2; typeId++) {
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

            assertThat(newStudy.getStudy().getStudyType().getId())
                .isEqualTo(completedStudy.getStudy().getStudyType().getId());
        }

        for (long typeId = 3; typeId <= 4; typeId++) {
            List<UserStudy> typeStudies = studiesByType.get(typeId);
            assertThat(typeStudies).hasSize(1)
                .withFailMessage("미완료 타입 %d의 스터디는 1개만 있어야 합니다", typeId);

            assertThat(typeStudies.get(0).isCompleted()).isFalse();
        }
    }
}
