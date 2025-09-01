package se.sowl.devlydomain.discussion.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.user.domain.User;

import static org.junit.jupiter.api.Assertions.*;

class DiscussionTest {

    @Test
    @DisplayName("Discussion 생성 시 기본 상태는 READY여야 한다")
    void defaultStatusShouldBeReady() {
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

        assertEquals(DiscussionStatus.READY, discussion.getStatus());
        assertEquals(0, discussion.getCurrentRound());
        assertNull(discussion.getStartedAt());
        assertFalse(discussion.isInProgress());
        assertFalse(discussion.isCompleted());
    }

    @Test
    @DisplayName("READY 상태에서 start() 호출 시 IN_PROGRESS로 변경되어야 한다")
    void startFromReadyState() {
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

        discussion.start();

        assertEquals(DiscussionStatus.IN_PROGRESS, discussion.getStatus());
        assertEquals(1, discussion.getCurrentRound());
        assertNotNull(discussion.getStartedAt());
        assertTrue(discussion.isInProgress());
        assertFalse(discussion.isCompleted());
    }

    @Test
    @DisplayName("이미 시작된 상태에서 start() 호출 시 예외가 발생해야 한다")
    void startFromInProgressStateShouldThrowException() {
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
        
        discussion.start();

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            discussion::start);
        assertEquals("Discussion can only be started from READY status", exception.getMessage());
    }

    @Test
    @DisplayName("IN_PROGRESS 상태에서 다음 라운드로 진행할 수 있어야 한다")
    void proceedToNextRound() {
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
        
        discussion.start(); // 1라운드
        discussion.proceedToNextRound(); // 2라운드

        assertEquals(2, discussion.getCurrentRound());
        assertTrue(discussion.isInProgress());
        assertFalse(discussion.isLastRound());
    }

    @Test
    @DisplayName("3라운드에서는 isLastRound()가 true를 반환해야 한다")
    void isLastRoundShouldReturnTrueAtThirdRound() {
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
        
        discussion.start(); // 1라운드
        discussion.proceedToNextRound(); // 2라운드
        discussion.proceedToNextRound(); // 3라운드

        assertEquals(3, discussion.getCurrentRound());
        assertTrue(discussion.isLastRound());
    }

    @Test
    @DisplayName("3라운드 이후 다음 라운드로 진행하려 할 때 예외가 발생해야 한다")
    void proceedBeyondThirdRoundShouldThrowException() {
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
        
        discussion.start();
        discussion.proceedToNextRound();
        discussion.proceedToNextRound();

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            discussion::proceedToNextRound);
        assertEquals("Cannot proceed beyond 3 rounds", exception.getMessage());
    }

    @Test
    @DisplayName("IN_PROGRESS 상태에서 complete() 호출 시 COMPLETED로 변경되어야 한다")
    void completeFromInProgressState() {
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
        
        discussion.start();

        String finalFeedback = "전반적으로 좋은 답변이었습니다.";
        Double overallScore = 8.5;
        
        discussion.complete(finalFeedback, overallScore);

        assertEquals(DiscussionStatus.COMPLETED, discussion.getStatus());
        assertEquals(finalFeedback, discussion.getFinalFeedback());
        assertEquals(overallScore, discussion.getOverallScore());
        assertNotNull(discussion.getCompletedAt());
        assertTrue(discussion.isCompleted());
        assertFalse(discussion.isInProgress());
    }

    @Test
    @DisplayName("READY 상태에서 complete() 호출 시 예외가 발생해야 한다")
    void completeFromReadyStateShouldThrowException() {
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

        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> discussion.complete("feedback", 8.0));
        assertEquals("Discussion must be in progress to complete", exception.getMessage());
    }

    @Test
    @DisplayName("fail() 호출 시 FAILED 상태로 변경되어야 한다")
    void failShouldChangeStatusToFailed() {
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
        
        discussion.start();
        discussion.fail();

        assertEquals(DiscussionStatus.FAILED, discussion.getStatus());
        assertNotNull(discussion.getCompletedAt());
        assertFalse(discussion.isInProgress());
        assertFalse(discussion.isCompleted());
    }
}