package se.sowl.devlydomain.discussion.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "discussion_topics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscussionTopic extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String difficulty;

    @Column(name = "initial_question", columnDefinition = "TEXT", nullable = false)
    private String initialQuestion;

    @Builder
    public DiscussionTopic(String title, String description, String category, String difficulty, String initialQuestion) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.initialQuestion = initialQuestion;
    }
}