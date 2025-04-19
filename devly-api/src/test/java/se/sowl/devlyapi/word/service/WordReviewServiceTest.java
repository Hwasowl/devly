package se.sowl.devlyapi.word.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.word.domain.WordReview;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WordReviewServiceTest extends MediumTest {

    @AfterEach
    void tearDown() {
        wordReviewRepository.deleteAllInBatch();
        userStudyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        wordRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
    }

    @Nested
    class WordReviews {
        @Test
        @DisplayName("단어 학습에 대한 결과를 저장할 수 있다.")
        void review() {
            // given
            DeveloperType developerType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
            User user = userRepository.save(createUser(1L, developerType, "박정수", "솔", "test@naver.com", "google"));
            StudyType studyType = studyTypeRepository.save(new StudyType("word", 100L));
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            wordRepository.saveAll(getBackendWordList(study));
            userStudyRepository.save(UserStudy.builder().user(user).study(study).scheduledAt(LocalDateTime.now()).build());

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
                if (review.getWord().getId() == 1L || review.getWord().getId() == 2L) {
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
            DeveloperType developerType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
            User user = userRepository.save(createUser(1L, developerType, "박정수", "솔", "test@naver.com", "google"));
            StudyType studyType = studyTypeRepository.save(new StudyType("word", 100L));
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            wordRepository.saveAll(getBackendWordList(study));
            userStudyRepository.save(UserStudy.builder().user(user).study(study).scheduledAt(LocalDateTime.now()).build());

            List<Long> correctIds = List.of(1L, 2L);
            List<Long> incorrectIds = List.of(3L, 4L, 5L);
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            // when
            List<Long> newCorrectIds = List.of(1L, 2L, 3L, 4L);
            wordReviewService.updateReview(study.getId(), user.getId(), newCorrectIds);

            // then
            List<WordReview> all = wordReviewRepository.findByStudyIdAndUserId(study.getId(), user.getId());

            all.forEach(review -> {
                if (review.getWord().getId() == 1L || review.getWord().getId() == 2L || review.getWord().getId() == 3L || review.getWord().getId() == 4L) {
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
            DeveloperType developerType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
            User user = userRepository.save(createUser(1L, developerType, "박정수", "솔", "test@naver.com", "google"));
            StudyType studyType = studyTypeRepository.save(new StudyType("word", 100L));
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            wordRepository.saveAll(getBackendWordList(study));
            userStudyRepository.save(UserStudy.builder().user(user).study(study).scheduledAt(LocalDateTime.now()).build());

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
            DeveloperType developerType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
            User user = userRepository.save(createUser(1L, developerType, "박정수", "솔", "test@naver.com", "google"));
            StudyType studyType = studyTypeRepository.save(new StudyType("word", 100L));
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            wordRepository.saveAll(getBackendWordList(study));
            userStudyRepository.save(UserStudy.builder().user(user).study(study).scheduledAt(LocalDateTime.now()).build());

            List<Long> correctIds = List.of(1L, 2L);
            List<Long> incorrectIds = List.of(3L, 4L, 5L);
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            // when
            List<Long> newCorrectIds = List.of();
            wordReviewService.updateReview(study.getId(), user.getId(), newCorrectIds);

            // then
            wordReviewRepository.findAll().forEach(review -> {
                if (review.getWord().getId() == 1L || review.getWord().getId() == 2L) {
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
            DeveloperType developerType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
            User user = userRepository.save(createUser(1L, developerType, "박정수", "솔", "test@naver.com", "google"));
            StudyType studyType = studyTypeRepository.save(new StudyType("word", 100L));
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            wordRepository.saveAll(getBackendWordList(study));
            userStudyRepository.save(UserStudy.builder().user(user).study(study).scheduledAt(LocalDateTime.now()).build());

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
