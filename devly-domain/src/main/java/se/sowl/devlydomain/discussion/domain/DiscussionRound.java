package se.sowl.devlydomain.discussion.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "discussion_rounds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiscussionRound extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id", nullable = false)
    private Discussion discussion;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "gpt_feedback", columnDefinition = "TEXT")
    private String gptFeedback;

    @Column(name = "round_score")
    private Double roundScore;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    @Builder
    public DiscussionRound(Discussion discussion, Integer roundNumber, String question) {
        this.discussion = discussion;
        this.roundNumber = roundNumber;
        this.question = question;
    }

    public void submitAnswer(String userAnswer) {
        if (this.userAnswer != null) {
            throw new IllegalStateException("Answer has already been submitted for this round");
        }
        this.userAnswer = userAnswer;
        this.answeredAt = LocalDateTime.now();
    }

    public void setFeedback(String gptFeedback, Double roundScore) {
        if (this.userAnswer == null) {
            throw new IllegalStateException("Cannot set feedback before user answer is submitted");
        }
        this.gptFeedback = gptFeedback;
        this.roundScore = roundScore;
    }

    public boolean isAnswered() {
        return this.userAnswer != null;
    }

    public boolean hasFeedback() {
        return this.gptFeedback != null;
    }
}