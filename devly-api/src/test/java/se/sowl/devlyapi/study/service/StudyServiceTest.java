package se.sowl.devlyapi.study.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeClassification;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class UserStudyServiceTest extends MediumTest {

    @Test
    @DisplayName("스터디 타입 별로 학습 정보를 반환한다.")
    void shouldReturnStudyTasksByType() {
        // given
        DeveloperType backendDeveloperType = createBackendDeveloperType();
        User user = createTestUser(backendDeveloperType);

        List<StudyType> allStudyTypes = createAllStudyTypes();
        StudyType wordStudyType = allStudyTypes.stream()
            .filter(studyType -> Objects.equals(studyType.getId(), StudyTypeClassification.WORD.getId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("단어 학습 타입을 찾을 수 없습니다."));
        Study wordStudy = createStudy(wordStudyType, backendDeveloperType);
        createBackendWords(wordStudy);
        assignUserToStudy(user, wordStudy);

        // 단어 학습 리뷰 생성 (4개 정답, 1개 오답)
        wordReviewService.createReview(
            wordStudy.getId(),
            user.getId(),
            List.of(1L, 2L, 3L, 4L),
            List.of(5L)
        );

        for (int i = 1; i < allStudyTypes.size(); i++) {
            Study study = createStudy(allStudyTypes.get(i), backendDeveloperType);
            UserStudy userStudy = assignUserToStudy(user, study);
            userStudy.complete();
            userStudyRepository.save(userStudy);
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
