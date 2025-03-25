package se.sowl.devlyapi.pr.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.pr.dto.review.PrCommentReviewResponse;
import se.sowl.devlyapi.pr.exception.PrCommentNotExistException;
import se.sowl.devlyapi.study.exception.StudyNotExistException;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.domain.PrReview;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTRequest;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;
import se.sowl.devlyexternal.common.gpt.GptPromptManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Profile("test")
class PrReviewServiceTest extends MediumTest {

    @MockBean
    private GPTClient gptClient;

    @MockBean
    private GptPromptManager gptPromptManager;

    @MockBean
    private PrReviewEntityParser prReviewEntityParser;

    @AfterEach
    void tearDown() {
        prCommentRepository.deleteAllInBatch();
        prRepository.deleteAllInBatch();
        prLabelRepository.deleteAllInBatch();
        userStudyRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Nested
    class ReviewPrComment {

        @Test
        @DisplayName("PR 코멘트에 대한 사용자 답변을 AI로 리뷰할 수 있다")
        void reviewPrComment_Success() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "테스트", "닉네임", "test@email.com", "github"));
            Study study = studyRepository.save(buildStudy(3L, 1L));
            Pr pr = prRepository.save(buildPr(study.getId()));
            PrComment prComment = prCommentRepository.save(buildPrComments(pr.getId()).get(0));

            String answer = "테스트 답변입니다.";

            mockReviewCreation(answer);

            // when
            PrCommentReviewResponse response = prReviewService.reviewPrComment(
                user.getId(), prComment.getId(), study.getId(), answer);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getReview()).isEqualTo("AI 리뷰 내용");
        }

        @Test
        @DisplayName("빈 답변으로 리뷰를 요청하면 예외가 발생한다")
        void reviewPrComment_EmptyAnswer() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "테스트", "닉네임", "test@email.com", "github"));
            Study study = studyRepository.save(buildStudy(3L, 1L));
            Pr pr = prRepository.save(buildPr(study.getId()));
            PrComment prComment = prCommentRepository.save(buildPrComments(pr.getId()).get(0));

            String emptyAnswer = "";

            // when & then
            assertThatThrownBy(() -> prReviewService.reviewPrComment(
                user.getId(), prComment.getId(), study.getId(), emptyAnswer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot review empty answer comment. Please check your argument");
        }

        @Test
        @DisplayName("존재하지 않는 PR 코멘트로 리뷰를 요청하면 예외가 발생한다")
        void reviewPrComment_NonExistentComment() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "테스트", "닉네임", "test@email.com", "github"));
            Study study = studyRepository.save(buildStudy(3L, 1L));
            Long nonExistentCommentId = 999L;
            String answer = "테스트 답변입니다.";

            // when & then
            assertThatThrownBy(() -> prReviewService.reviewPrComment(
                user.getId(), nonExistentCommentId, study.getId(), answer))
                .isInstanceOf(PrCommentNotExistException.class);
        }

        @Test
        @DisplayName("존재하지 않는 스터디로 리뷰를 요청하면 예외가 발생한다")
        void reviewPrComment_NonExistentStudy() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "테스트", "닉네임", "test@email.com", "github"));
            Study study = studyRepository.save(buildStudy(3L, 1L));
            Pr pr = prRepository.save(buildPr(study.getId()));
            PrComment prComment = prCommentRepository.save(buildPrComments(pr.getId()).get(0));

            Long nonExistentStudyId = 999L;
            String answer = "테스트 답변입니다.";

            // when & then
            assertThatThrownBy(() -> prReviewService.reviewPrComment(
                user.getId(), prComment.getId(), nonExistentStudyId, answer))
                .isInstanceOf(StudyNotExistException.class);
        }

        private void mockReviewCreation(String answer) {
            GPTResponse gptResponse = mock(GPTResponse.class);
            when(gptResponse.getContent()).thenReturn("AI 응답 내용");

            PrReview prReview = PrReview.builder()
                .review("AI 리뷰 내용")
                .build();

            GPTRequest mockRequest = mock(GPTRequest.class);

            when(gptClient.generate(any())).thenReturn(gptResponse);
            doNothing().when(gptPromptManager).addRolePrompt(anyLong(), anyLong(), any(StringBuilder.class));
            when(prReviewEntityParser.createGPTRequest(anyString())).thenReturn(mockRequest);
            when(prReviewEntityParser.parseEntity(any(), anyString())).thenReturn(prReview);
        }
    }
}
