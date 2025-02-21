package se.sowl.devlyapi.word.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.word.domain.WordReview;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class WordReviewServiceTest extends MediumTest {

    @AfterEach
    void tearDown() {
        wordRepository.deleteAllInBatch();
        userStudyRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Nested
    class WordReviews {
        @Test
        @DisplayName("단어 학습에 대한 결과를 저장할 수 있다.")
        void review() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "박정수", "솔", "test@naver.com", "google"));
            Study study = studyRepository.save(buildStudy(2L, 1L));
            wordRepository.saveAll(getBackendWordList(study.getId()));
            userStudyRepository.save(UserStudy.builder().userId(user.getId()).study(study).scheduledAt(LocalDateTime.now()).build());

            // when
            wordReviewService.createReview(study.getId(), user.getId(), List.of(1L, 2L), List.of(3L, 4L, 5L));

            // then
            userStudyRepository.findByUserIdAndStudyId(user.getId(), study.getId())
                .ifPresentOrElse(
                    UserStudy::complete,
                    () -> {
                        throw new NotAssignmentWordStudyException();
                    }
                );
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
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            // when
            List<Long> newCorrectIds = List.of(1L, 2L, 3L, 4L);
            wordReviewService.updateReview(study.getId(), user.getId(), newCorrectIds);

            // then
            List<WordReview> all = wordReviewRepository.findAll();
            for(WordReview r : all) {
                System.out.println(r.getWordId() + " " + r.isCorrect());
            }
            wordReviewRepository.findAll().forEach(review -> {
                if (review.getWordId() == 1L || review.getWordId() == 2L || review.getWordId() == 3L || review.getWordId() == 4L) {
                    assertThat(review.isCorrect()).isTrue();
                } else {
                    assertThat(review.isCorrect()).isFalse();
                }
            });
        }

        @Test
        @DisplayName("만약 처음 제출한 답이 모두 정답이라면 단어 학습을 완료 처리한다.")
        void complete() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "박정수", "솔", "test@naver.com", "google"));
            Study study = studyRepository.save(buildStudy(2L, 1L));
            wordRepository.saveAll(getBackendWordList(study.getId()));
            userStudyRepository.save(UserStudy.builder().userId(user.getId()).study(study).scheduledAt(LocalDateTime.now()).build());

            List<Long> correctIds = List.of(1L, 2L, 3L, 4L, 5L);
            List<Long> incorrectIds = List.of();

            // when
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            // then
            userStudyRepository.findByUserIdAndStudyId(user.getId(), study.getId())
                .ifPresentOrElse(
                    UserStudy::complete,
                    () -> {
                        throw new NotAssignmentWordStudyException();
                    }
                );
        }

        @Test
        @DisplayName("리뷰 후 맞춘게 없는 경우 현 상태를 유지해야 한다.")
        void noAnswerAfterReview() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "박정수", "솔", "test@naver.com", "google"));
            Study study = studyRepository.save(buildStudy(2L, 1L));
            wordRepository.saveAll(getBackendWordList(study.getId()));
            userStudyRepository.save(UserStudy.builder().userId(user.getId()).study(study).scheduledAt(LocalDateTime.now()).build());

            List<Long> correctIds = List.of(1L, 2L);
            List<Long> incorrectIds = List.of(3L, 4L, 5L);
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            // when
            List<Long> newCorrectIds = List.of();
            wordReviewService.updateReview(study.getId(), user.getId(), newCorrectIds);

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
        void completeAfterInitial() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "박정수", "솔", "test@naver.com", "google"));
            Study study = studyRepository.save(buildStudy(2L, 1L));
            wordRepository.saveAll(getBackendWordList(study.getId()));
            userStudyRepository.save(UserStudy.builder().userId(user.getId()).study(study).scheduledAt(LocalDateTime.now()).build());

            List<Long> correctIds = List.of(1L, 2L);
            List<Long> incorrectIds = List.of(3L, 4L, 5L);
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            List<Long> newCorrectIds = List.of(1L, 2L, 3L, 4L, 5L);

            // when
            wordReviewService.updateReview(study.getId(), user.getId(), newCorrectIds);

            // then
            userStudyRepository.findByUserIdAndStudyId(user.getId(), study.getId())
                .ifPresentOrElse(
                    UserStudy::complete,
                    () -> {
                        throw new NotAssignmentWordStudyException();
                    }
                );
        }
    }

}
