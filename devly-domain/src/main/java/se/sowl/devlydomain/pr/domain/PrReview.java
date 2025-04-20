package se.sowl.devlydomain.pr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.user.domain.User;

@Entity
@Table(name = "pr_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrReview extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_comment_id")
    private PrComment prComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String answer;

    private String review;

    @Builder
    public PrReview(User user, PrComment comment, String answer, String review) {
        this.user = user;
        this.prComment = comment;
        this.answer = answer;
        this.review = review;
    }
}
