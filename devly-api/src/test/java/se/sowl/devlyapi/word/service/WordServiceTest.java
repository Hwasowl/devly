package se.sowl.devlyapi.word.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.dto.WordResponse;
import se.sowl.devlyapi.word.dto.reviews.WordReviewResponse;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.user.domain.User;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WordServiceTest extends MediumTest {

    @Nested
    class GetTasks {
        @Test
        @DisplayName("학습 ID로 소속 단어 목록을 조회할 수 있다.")
        void shouldGetWordListByStudyId() {
            // given
            DeveloperType developerType = createBackendDeveloperType();
            User user = createTestUser(1L, developerType, "박정수", "솔", "hwasowl598@gmail.com", "google");
            StudyType studyType = createWordStudyType();
            Study study = createStudy(studyType, developerType);
            createBackendWords(study);
            assignUserToStudy(user, study);

            // when
            WordListOfStudyResponse list = wordService.getListResponse(user.getId(), study.getId());

            // then
            List<WordResponse> words = list.getWords();
            assertThat(words).hasSize(5);
            assertThat(words.get(0).getWord()).isEqualTo("implementation");
            assertThat(words.get(0).getMeaning()).isEqualTo("구현, 실행");
            assertThat(words.get(1).getWord()).isEqualTo("polymorphism");
            assertThat(words.get(1).getMeaning()).isEqualTo("다형성");
            assertThat(words.get(2).getWord()).isEqualTo("middleware");
            assertThat(words.get(2).getMeaning()).isEqualTo("미들웨어");
        }

        @Test
        @DisplayName("배정되지 않은 학습 ID로 소속 단어 목록을 조회하면 NotAssignmentWordStudyException이 발생한다.")
        void shouldThrowExceptionWhenStudyNotAssigned() {
            // given
            DeveloperType developerType = createBackendDeveloperType();
            createTestUser(1L, developerType, "박정수", "솔", "hwasowl598@gmail.com", "google");
            StudyType studyType = createWordStudyType();
            Study study = createStudy(studyType, developerType);
            createBackendWords(study);
            // 사용자에게 학습 할당하지 않음

            // when & then
            NotAssignmentWordStudyException exception = assertThrows(
                NotAssignmentWordStudyException.class,
                () -> wordService.getListResponse(1L, 1L));
            assertEquals("아직 학습할 수 없습니다.", exception.getMessage());
        }
    }

    @Nested
    class GetWordReviews {
        @Test
        @DisplayName("단어 학습 결과를 조회할 수 있다.")
        void shouldGetWordReviewResults() {
            // given
            DeveloperType developerType = createBackendDeveloperType();
            User user = createTestUser(1L, developerType, "박정수", "솔", "test@naver.com", "google");
            StudyType studyType = createWordStudyType();
            Study study = createStudy(studyType, developerType);
            assignUserToStudy(user, study);
            createBackendWords(study);

            List<Long> correctIds = List.of(1L, 2L);
            List<Long> incorrectIds = List.of(3L, 4L, 5L);
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            // when
            WordReviewResponse wordReviews = wordService.getWordReviewsResponse(study.getId(), user.getId());

            // then
            assertThat(wordReviews.getCorrectIds()).containsExactly(1L, 2L);
            assertThat(wordReviews.getIncorrectIds()).containsExactly(3L, 4L, 5L);
        }
    }
}
