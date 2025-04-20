package se.sowl.devlyapi.word.service;

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

import java.util.List;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WordReviewServiceTest extends MediumTest {

    @Nested
    class WordReviews {
        @Test
        @DisplayName("단어 학습에 대한 결과를 저장할 수 있다.")
        void shouldSaveWordReviewResults() {
            // given
            DeveloperType developerType = createBackendDeveloperType();
            User user = createTestUser(1L, developerType, "박정수", "솔", "test@naver.com", "google");
            StudyType studyType = createWordStudyType();
            Study study = createStudy(studyType, developerType);
            createBackendWords(study);
            assignUserToStudy(user, study);

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

            List<WordReview> reviews = wordReviewRepository.findAll();
            assertWordReviews(reviews, List.of(1L, 2L));
        }

        @Test
        @DisplayName("이미 학습한 단어 학습에 대한 결과를 업데이트 할 수 있다.")
        void shouldUpdateExistingReviews() {
            // given
            DeveloperType developerType = createBackendDeveloperType();
            User user = createTestUser(1L, developerType, "박정수", "솔", "test@naver.com", "google");
            StudyType studyType = createWordStudyType();
            Study study = createStudy(studyType, developerType);
            createBackendWords(study);
            UserStudy userStudy = assignUserToStudy(user, study);

            List<Long> correctIds = List.of(1L, 2L);
            List<Long> incorrectIds = List.of(3L, 4L, 5L);
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            // when
            List<Long> newCorrectIds = List.of(1L, 2L, 3L, 4L);
            wordReviewService.updateReview(study.getId(), user.getId(), newCorrectIds);

            // then
            List<WordReview> reviews = wordReviewRepository.findByUserStudyId(userStudy.getId());
            assertWordReviews(reviews, newCorrectIds);
        }

        @Test
        @DisplayName("만약 처음 제출한 답이 모두 정답이라면 단어 학습을 완료 처리한다.")
        void shouldCompleteStudyWhenAllAnswersCorrect() {
            // given
            DeveloperType developerType = createBackendDeveloperType();
            User user = createTestUser(1L, developerType, "박정수", "솔", "test@naver.com", "google");
            StudyType studyType = createWordStudyType();
            Study study = createStudy(studyType, developerType);
            createBackendWords(study);
            assignUserToStudy(user, study);

            List<Long> correctIds = List.of(1L, 2L, 3L, 4L, 5L);
            List<Long> incorrectIds = List.of();

            // when
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            // then
            assertStudyCompleted(user, study);
        }

        @Test
        @DisplayName("리뷰 후 맞춘게 없는 경우 현 상태를 유지해야 한다.")
        void shouldMaintainStateWhenNoCorrectAnswersAfterReview() {
            // given
            DeveloperType developerType = createBackendDeveloperType();
            User user = createTestUser(1L, developerType, "박정수", "솔", "test@naver.com", "google");
            StudyType studyType = createWordStudyType();
            Study study = createStudy(studyType, developerType);
            createBackendWords(study);
            assignUserToStudy(user, study);

            List<Long> correctIds = List.of(1L, 2L);
            List<Long> incorrectIds = List.of(3L, 4L, 5L);
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            // when
            List<Long> newCorrectIds = List.of();
            wordReviewService.updateReview(study.getId(), user.getId(), newCorrectIds);

            // then
            List<WordReview> reviews = wordReviewRepository.findAll();
            assertWordReviews(reviews, List.of(1L, 2L));
        }

        @Test
        @DisplayName("이미 학습한 단어 학습에 대한 결과를 업데이트 할 수 있다.")
        void shouldCompleteStudyAfterUpdateWithAllCorrectAnswers() {
            // given
            DeveloperType developerType = createBackendDeveloperType();
            User user = createTestUser(1L, developerType, "박정수", "솔", "test@naver.com", "google");
            StudyType studyType = createWordStudyType();
            Study study = createStudy(studyType, developerType);
            createBackendWords(study);
            assignUserToStudy(user, study);

            List<Long> correctIds = List.of(1L, 2L);
            List<Long> incorrectIds = List.of(3L, 4L, 5L);
            wordReviewService.createReview(study.getId(), user.getId(), correctIds, incorrectIds);

            // when
            List<Long> newCorrectIds = List.of(1L, 2L, 3L, 4L, 5L);
            wordReviewService.updateReview(study.getId(), user.getId(), newCorrectIds);

            // then
            assertStudyCompleted(user, study);
        }
    }
}
