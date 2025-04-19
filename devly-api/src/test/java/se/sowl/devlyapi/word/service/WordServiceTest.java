package se.sowl.devlyapi.word.service;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
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
import se.sowl.devlydomain.user.domain.UserStudy;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WordServiceTest extends MediumTest {

    @AfterEach
    void tearDown() {
        wordReviewRepository.deleteAllInBatch();
        userStudyRepository.deleteAllInBatch();
        wordRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Nested
    class GetTasks {
        @Test
        @DisplayName("학습 ID로 소속 단어 목록을 조회할 수 있다.")
        void get() {
            // given
            DeveloperType developerType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
            User user = userRepository.save(createUser(1L, developerType, "박정수", "솔", "hwasowl598@gmail.com", "google"));
            StudyType studyType = studyTypeRepository.save(new StudyType("word", 100L));
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            wordRepository.saveAll(getBackendWordList(study));
            userStudyRepository.save(UserStudy.builder().user(user).study(study).scheduledAt(LocalDateTime.now()).build());

            // when
            WordListOfStudyResponse list = wordService.getListResponse(user.getId(), study.getId());
            List<WordResponse> words = list.getWords();

            // then
            assertThat(words).hasSize(5);
            AssertionsForClassTypes.assertThat(words.get(0).getWord()).isEqualTo("implementation");
            AssertionsForClassTypes.assertThat(words.get(0).getMeaning()).isEqualTo("구현, 실행");
            AssertionsForClassTypes.assertThat(words.get(1).getWord()).isEqualTo("polymorphism");
            AssertionsForClassTypes.assertThat(words.get(1).getMeaning()).isEqualTo("다형성");
            AssertionsForClassTypes.assertThat(words.get(2).getWord()).isEqualTo("middleware");
            AssertionsForClassTypes.assertThat(words.get(2).getMeaning()).isEqualTo("미들웨어");
        }

        @Test
        @DisplayName("배정되지 않은 학습 ID로 소속 단어 목록을 조회하면 NotAssignmentWordStudyException이 발생한다.")
        void notAssignment () {
            // given
            DeveloperType developerType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
            userRepository.save(createUser(1L, developerType, "박정수", "솔", "hwasowl598@gmail.com", "google"));
            StudyType studyType = studyTypeRepository.save(new StudyType("word", 100L));
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            wordRepository.saveAll(getBackendWordList(study));

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
        void getWordReviews() {
            // given
            DeveloperType developerType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
            User user = userRepository.save(createUser(1L, developerType, "박정수", "솔", "test@naver.com", "google"));
            StudyType studyType = studyTypeRepository.save(new StudyType("word", 100L));
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            userStudyRepository.save(UserStudy.builder().user(user).study(study).scheduledAt(LocalDateTime.now()).build());
            wordRepository.saveAll(getBackendWordList(study));
            wordReviewService.createReview(study.getId(), user.getId(), List.of(1L, 2L), List.of(3L, 4L, 5L));

            // when
            WordReviewResponse wordReviews = wordService.getWordReviewsResponse(study.getId(), user.getId());

            // then
            assertThat(wordReviews.getCorrectIds()).containsExactly(1L, 2L);
            assertThat(wordReviews.getIncorrectIds()).containsExactly(3L, 4L, 5L);
        }
    }
}
