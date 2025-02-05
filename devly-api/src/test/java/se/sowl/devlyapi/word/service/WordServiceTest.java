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
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlydomain.study.domain.Study;
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
        wordRepository.deleteAllInBatch();
        userStudyRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Nested
    class GetList {
        @Test
        @DisplayName("학습 ID로 소속 단어 목록을 조회할 수 있다.")
        void get() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "박정수", "솔", "hwasowl598@gmail.com", "google"));
            Study study = studyRepository.save(buildStudy(2L, 2L));
            wordRepository.saveAll(getBackendWordList(study.getId()));
            userStudyRepository.save(UserStudy.builder().userId(user.getId()).study(study).scheduledAt(LocalDateTime.now()).build());

            // when
            WordListOfStudyResponse list = wordService.getList(user.getId(), study.getId());
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
            userRepository.save(createUser(1L, 1L, "박정수", "솔", "hwasowl598@gmail.com", "google"));
            Study study = studyRepository.save(buildStudy(2L, 2L));
            wordRepository.saveAll(getBackendWordList(study.getId()));
            // 단어와 스터디는 있지만 유저에게 할당되지 않은 상태

            // when & then
            NotAssignmentWordStudyException exception = assertThrows(
                NotAssignmentWordStudyException.class,
                () -> wordService.getList(1L, 1L));
            assertEquals("아직 학습할 수 없습니다.", exception.getMessage());
        }
    }

    @Nested
    class Review {
        @Test
        @DisplayName("단어 학습에 대한 결과를 저장할 수 있다.")
        void review() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "박정수", "솔", "test@naver.com", "google"));
            Study study = studyRepository.save(buildStudy(2L, 1L));
            wordRepository.saveAll(getBackendWordList(study.getId()));
            userStudyRepository.save(UserStudy.builder().userId(user.getId()).study(study).scheduledAt(LocalDateTime.now()).build());

            // when
            wordService.review(study.getId(), user.getId(), List.of(1L, 2L), List.of(3L, 4L, 5L));

            // then
            wordReviewRepository.findAll().forEach(review -> {
                if (review.getWordId() == 1L || review.getWordId() == 2L) {
                    assertThat(review.isCorrect()).isTrue();
                } else {
                    assertThat(review.isCorrect()).isFalse();
                }
            });
        }

        @Test
        @DisplayName("이미 학습한 단어 학습에 대한 결과를 업데이트 할 수 있다.")
        void updateReview() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "박정수", "솔", "test@naver.com", "google"));
            Study study = studyRepository.save(buildStudy(2L, 1L));
            wordRepository.saveAll(getBackendWordList(study.getId()));
            userStudyRepository.save(UserStudy.builder().userId(user.getId()).study(study).scheduledAt(LocalDateTime.now()).build());

            List<Long> correctIds = List.of(1L, 2L);
            List<Long> incorrectIds = List.of(3L, 4L, 5L);
            wordService.review(study.getId(), user.getId(), correctIds, incorrectIds);

            List<Long> newCorrectIds = List.of(1L, 2L, 3L, 4L);
            List<Long> newIncorrectIds = List.of(5L);

            // when
            wordService.review(study.getId(), user.getId(), newCorrectIds, newIncorrectIds);

            // then
            wordReviewRepository.findAll().forEach(review -> {
                if (review.getWordId() == 1L || review.getWordId() == 2L || review.getWordId() == 3L || review.getWordId() == 4L) {
                    assertThat(review.isCorrect()).isTrue();
                } else {
                    assertThat(review.isCorrect()).isFalse();
                }
            });
        }
    }
}
