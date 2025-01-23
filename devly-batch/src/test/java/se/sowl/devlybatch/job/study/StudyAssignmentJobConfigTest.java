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

import java.time.LocalDate;
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
        LocalDateTime yesterday = LocalDateTime.now()
            .minusDays(1)
            .withHour(12)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);

        for (int typeId = 0; typeId < 4; typeId++) {
            allStudies.addAll(createOrderedStudies((long) typeId, 1L, STUDIES_PER_TYPE));
        }

        for (Long userId : USER_IDS) {
            UserStudy completed1 = createUserStudy(userId, findFirstStudyByType(allStudies, 0L).getId(), true, yesterday);
            UserStudy completed2 = createUserStudy(userId, findFirstStudyByType(allStudies, 1L).getId(), true, yesterday);
            UserStudy incomplete1 = createUserStudy(userId, findFirstStudyByType(allStudies, 2L).getId(), false, null);
            UserStudy incomplete2 = createUserStudy(userId, findFirstStudyByType(allStudies, 3L).getId(), false, null);
            userStudyRepository.saveAll(Arrays.asList(completed1, completed2, incomplete1, incomplete2));
        }

        // when
        JobExecution execution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        for (Long userId : USER_IDS) {
            List<UserStudy> userStudies = userStudyRepository.findAllByUserId(userId);

            Map<Long, List<UserStudy>> studiesByType = userStudies.stream()
                .collect(Collectors.groupingBy(
                    us -> studyRepository.findById(us.getStudyId()).orElseThrow().getTypeId()
                ));

            for (long typeId : Arrays.asList(0L, 1L)) {
                List<UserStudy> typeStudies = studiesByType.get(typeId);
                assertThat(typeStudies).hasSize(2);

                UserStudy completedStudy = typeStudies.stream()
                    .filter(UserStudy::isCompleted)
                    .findFirst()
                    .orElseThrow(AssertionError::new);

                UserStudy newStudy = typeStudies.stream()
                    .filter(us -> !us.isCompleted())
                    .findFirst()
                    .orElseThrow(AssertionError::new);

                // 새로운 스터디의 ID가 완료된 스터디의 ID보다 커야 함 (도메인 규칙)
                assertThat(newStudy.getStudyId()).isGreaterThan(completedStudy.getStudyId());

                Study completedStudyEntity = studyRepository.findById(completedStudy.getStudyId()).orElseThrow();
                Study newStudyEntity = studyRepository.findById(newStudy.getStudyId()).orElseThrow();
                assertThat(newStudyEntity.getTypeId()).isEqualTo(completedStudyEntity.getTypeId());
            }

            for (long typeId : Arrays.asList(2L, 3L)) {
                List<UserStudy> typeStudies = studiesByType.get(typeId);
                assertThat(typeStudies).hasSize(1);
                assertThat(typeStudies.get(0).isCompleted()).isFalse();
            }
        }
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

    private List<Study> createStudies(Long typeId, Long devTypeId, int count) {
        List<Study> studies = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            studies.add(Study.builder()
                .typeId(typeId)
                .developerTypeId(devTypeId)
                .build()
            );
        }
        return studyRepository.saveAll(studies);
    }

    private void createStudies() {
        createStudies(1L, 1L, 2);
    }

    private UserStudy createUserStudy(Long userId, Long studyId, boolean completed, LocalDateTime completedAt) {
        UserStudy userStudy = UserStudy.builder()
            .userId(userId)
            .studyId(studyId)
            .scheduledAt(LocalDate.now().minusDays(1))  // 어제 날짜로 설정
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


