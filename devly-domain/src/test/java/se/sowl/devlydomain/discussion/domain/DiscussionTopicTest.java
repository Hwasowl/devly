package se.sowl.devlydomain.discussion.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscussionTopicTest {

    @Test
    @DisplayName("DiscussionTopic이 올바르게 생성되어야 한다")
    void createDiscussionTopic() {
        String title = "Spring Framework";
        String description = "Spring 프레임워크에 대한 기본적인 면접 질문들";
        String category = "BACKEND";
        String difficulty = "INTERMEDIATE";
        String initialQuestion = "Spring의 IoC 컨테이너에 대해 설명해주세요.";

        DiscussionTopic topic = DiscussionTopic.builder()
            .title(title)
            .description(description)
            .category(category)
            .difficulty(difficulty)
            .initialQuestion(initialQuestion)
            .build();

        assertEquals(title, topic.getTitle());
        assertEquals(description, topic.getDescription());
        assertEquals(category, topic.getCategory());
        assertEquals(difficulty, topic.getDifficulty());
        assertEquals(initialQuestion, topic.getInitialQuestion());
    }

    @Test
    @DisplayName("필수 필드들이 올바르게 설정되어야 한다")
    void requiredFieldsShouldBeSet() {
        DiscussionTopic topic = DiscussionTopic.builder()
            .title("Java Basics")
            .category("BACKEND")
            .difficulty("BEGINNER")
            .initialQuestion("Java의 특징에 대해 설명해주세요.")
            .build();

        assertNotNull(topic.getTitle());
        assertNotNull(topic.getCategory());
        assertNotNull(topic.getDifficulty());
        assertNotNull(topic.getInitialQuestion());
    }

    @Test
    @DisplayName("description은 선택적 필드여야 한다")
    void descriptionShouldBeOptional() {
        DiscussionTopic topic = DiscussionTopic.builder()
            .title("Java Basics")
            .category("BACKEND")
            .difficulty("BEGINNER")
            .initialQuestion("Java의 특징에 대해 설명해주세요.")
            .build();

        assertNull(topic.getDescription());
    }
}