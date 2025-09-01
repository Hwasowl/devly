package se.sowl.devlydomain.discussion.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.user.domain.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "discussions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Discussion extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private DiscussionTopic topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscussionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "current_round")
    private Integer currentRound;

    @Column(name = "final_feedback", columnDefinition = "TEXT")
    private String finalFeedback;

    @Column(name = "overall_score")
    private Double overallScore;

    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiscussionRound> rounds = new ArrayList<>();

    @Builder
    public Discussion(User user, DiscussionTopic topic) {
        this.user = user;
        this.topic = topic;
        this.status = DiscussionStatus.READY;
        this.currentRound = 0;
    }

    public void start() {
        if (this.status != DiscussionStatus.READY) {
            throw new IllegalStateException("Discussion can only be started from READY status");
        }
        this.status = DiscussionStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
        this.currentRound = 1;
    }

    public void proceedToNextRound() {
        if (this.status != DiscussionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Discussion must be in progress to proceed to next round");
        }
        if (this.currentRound >= 3) {
            throw new IllegalStateException("Cannot proceed beyond 3 rounds");
        }
        this.currentRound++;
    }

    public void complete(String finalFeedback, Double overallScore) {
        if (this.status != DiscussionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Discussion must be in progress to complete");
        }
        this.status = DiscussionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.finalFeedback = finalFeedback;
        this.overallScore = overallScore;
    }

    public void fail() {
        this.status = DiscussionStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return this.status == DiscussionStatus.COMPLETED;
    }

    public boolean isInProgress() {
        return this.status == DiscussionStatus.IN_PROGRESS;
    }

    public boolean isLastRound() {
        return this.currentRound == 3;
    }
}