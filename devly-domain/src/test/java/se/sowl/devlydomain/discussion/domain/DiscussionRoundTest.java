package se.sowl.devlydomain.discussion.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.sowl.devlydomain.user.domain.User;

import static org.junit.jupiter.api.Assertions.*;

class DiscussionRoundTest {

    @Test
    @DisplayName("DiscussionRound 생성 시 답변과 피드백이 없는 상태여야 한다")
    void initialStateShouldBeEmpty() {
        User user = User.builder().email("test@test.com").build();
        DiscussionTopic topic = DiscussionTopic.builder()
            .title("Spring Framework")
            .description("Spring 기초 면접")
            .category("BACKEND")
            .difficulty("INTERMEDIATE")
            .initialQuestion("Spring의 IoC에 대해 설명해주세요.")
            .build();

        Discussion discussion = Discussion.builder()
            .user(user)
            .topic(topic)
            .build();

        DiscussionRound round = DiscussionRound.builder()
            .discussion(discussion)
            .roundNumber(1)
            .question("Spring의 IoC에 대해 설명해주세요.")
            .build();

        assertEquals(1, round.getRoundNumber());
        assertEquals("Spring의 IoC에 대해 설명해주세요.", round.getQuestion());
        assertNull(round.getUserAnswer());
        assertNull(round.getGptFeedback());
        assertNull(round.getRoundScore());
        assertNull(round.getAnsweredAt());
        assertFalse(round.isAnswered());
        assertFalse(round.hasFeedback());
    }

    @Test
    @DisplayName("답변 제출 시 상태가 올바르게 업데이트되어야 한다")
    void submitAnswerShouldUpdateState() {
        User user = User.builder().email("test@test.com").build();
        DiscussionTopic topic = DiscussionTopic.builder()
            .title("Spring Framework")
            .description("Spring 기초 면접")
            .category("BACKEND")
            .difficulty("INTERMEDIATE")
            .initialQuestion("Spring의 IoC에 대해 설명해주세요.")
            .build();

        Discussion discussion = Discussion.builder()
            .user(user)
            .topic(topic)
            .build();

        DiscussionRound round = DiscussionRound.builder()
            .discussion(discussion)
            .roundNumber(1)
            .question("Spring의 IoC에 대해 설명해주세요.")
            .build();

        String answer = "IoC는 Inversion of Control의 줄임말로 제어의 역전을 의미합니다.";
        round.submitAnswer(answer);

        assertEquals(answer, round.getUserAnswer());
        assertNotNull(round.getAnsweredAt());
        assertTrue(round.isAnswered());
        assertFalse(round.hasFeedback());
    }

    @Test
    @DisplayName("이미 답변이 제출된 라운드에 다시 답변 제출 시 예외가 발생해야 한다")
    void submitAnswerTwiceShouldThrowException() {
        User user = User.builder().email("test@test.com").build();
        DiscussionTopic topic = DiscussionTopic.builder()
            .title("Spring Framework")
            .description("Spring 기초 면접")
            .category("BACKEND")
            .difficulty("INTERMEDIATE")
            .initialQuestion("Spring의 IoC에 대해 설명해주세요.")
            .build();

        Discussion discussion = Discussion.builder()
            .user(user)
            .topic(topic)
            .build();

        DiscussionRound round = DiscussionRound.builder()
            .discussion(discussion)
            .roundNumber(1)
            .question("Spring의 IoC에 대해 설명해주세요.")
            .build();

        round.submitAnswer("첫 번째 답변");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> round.submitAnswer("두 번째 답변"));
        assertEquals("Answer has already been submitted for this round", exception.getMessage());
    }

    @Test
    @DisplayName("답변 제출 후 피드백 설정이 가능해야 한다")
    void setFeedbackAfterAnswer() {
        User user = User.builder().email("test@test.com").build();
        DiscussionTopic topic = DiscussionTopic.builder()
            .title("Spring Framework")
            .description("Spring 기초 면접")
            .category("BACKEND")
            .difficulty("INTERMEDIATE")
            .initialQuestion("Spring의 IoC에 대해 설명해주세요.")
            .build();

        Discussion discussion = Discussion.builder()
            .user(user)
            .topic(topic)
            .build();

        DiscussionRound round = DiscussionRound.builder()
            .discussion(discussion)
            .roundNumber(1)
            .question("Spring의 IoC에 대해 설명해주세요.")
            .build();

        round.submitAnswer("IoC에 대한 답변");
        
        String feedback = "좋은 설명입니다. 구체적인 예시가 있다면 더 좋겠습니다.";
        Double score = 8.5;
        round.setFeedback(feedback, score);

        assertEquals(feedback, round.getGptFeedback());
        assertEquals(score, round.getRoundScore());
        assertTrue(round.hasFeedback());
    }

    @Test
    @DisplayName("답변 제출 전에 피드백 설정 시 예외가 발생해야 한다")
    void setFeedbackBeforeAnswerShouldThrowException() {
        User user = User.builder().email("test@test.com").build();
        DiscussionTopic topic = DiscussionTopic.builder()
            .title("Spring Framework")
            .description("Spring 기초 면접")
            .category("BACKEND")
            .difficulty("INTERMEDIATE")
            .initialQuestion("Spring의 IoC에 대해 설명해주세요.")
            .build();

        Discussion discussion = Discussion.builder()
            .user(user)
            .topic(topic)
            .build();

        DiscussionRound round = DiscussionRound.builder()
            .discussion(discussion)
            .roundNumber(1)
            .question("Spring의 IoC에 대해 설명해주세요.")
            .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> round.setFeedback("피드백", 8.0));
        assertEquals("Cannot set feedback before user answer is submitted", exception.getMessage());
    }
}