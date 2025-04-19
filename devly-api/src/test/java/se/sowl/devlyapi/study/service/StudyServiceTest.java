package se.sowl.devlyapi.study.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class UserStudyServiceTest extends MediumTest {

    @Autowired
    private DeveloperTypeRepository developerTypeRepository;

    @Test
    @DisplayName("스터디 타입 별로 학습 정보를 반환한다.")
    void tasksStatus() {
        // given
        List<DeveloperType> developerTypes = developerTypeRepository.saveAll(getDeveloperTypes());
        DeveloperType backendDeveloperType = developerTypes.stream().filter(developerType -> developerType.getName().equals("Backend Developer")).findFirst().orElseThrow();

        User user = userRepository.save(createUser(1L, backendDeveloperType, "박정수", "솔", "hwasowl598@gmail.com", "google"));

        // TODO: 추후 학습 도메인이 추가된다면 부가 학습을 추가해 대응해야만 한다.
        List<StudyType> studyTypes = studyTypeRepository.saveAll(getStudyTypes());
        StudyType wordTypeId = studyTypes.stream().filter(studyType -> studyType.getName().equals("word")).findFirst().orElseThrow();
        Study study = studyRepository.save(buildStudy(wordTypeId, backendDeveloperType));
        wordRepository.saveAll(getBackendWordList(study));
        userStudyRepository.save(UserStudy.builder().user(user).study(study).scheduledAt(LocalDateTime.now()).build());
        wordReviewService.createReview(study.getId(), user.getId(), List.of(1L, 2L, 3L, 4L), List.of(5L)); // 리뷰 4개 정답 1개 오답

        // else type studies
        for (int i = 1; i <= 3; i++) {
            StudyType studyType = studyTypes.get(i);
            Study study2 = studyRepository.save(buildStudy(studyType, backendDeveloperType));
            UserStudy build = UserStudy.builder().user(user).study(study2).scheduledAt(LocalDateTime.now()).build();
            build.complete();
            userStudyRepository.save(build);
        }

        // when
        UserStudyTasksResponse tasks = userStudyService.getUserStudyTasks(user.getId());

        // then
        assertThat(tasks.getWord()).isNotNull();
        assertThat(tasks.getWord().getTotal()).isEqualTo(1);
        assertThat(tasks.getWord().isCompleted()).isFalse();
        assertThat(tasks.getKnowledge()).isNotNull();
        assertThat(tasks.getKnowledge().isCompleted()).isTrue();
        assertThat(tasks.getPr()).isNotNull();
        assertThat(tasks.getPr().isCompleted()).isTrue();
        assertThat(tasks.getDiscussion()).isNotNull();
        assertThat(tasks.getDiscussion().isCompleted()).isTrue();
    }

}
