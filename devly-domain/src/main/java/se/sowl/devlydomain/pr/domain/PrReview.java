package se.sowl.devlydomain.pr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "pr_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrReview extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pull_comment_id")
    private Long prCommentId;

    @Column(name = "user_id")
    private Long userId;

    private String answer;

    private String review;

    @Builder
    public PrReview(Long userId, Long prCommentId, String answer, String review) {
        this.userId = userId;
        this.prCommentId = prCommentId;
        this.answer = answer;
        this.review = review;
    }
}
