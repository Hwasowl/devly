package se.sowl.devlydomain.pr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    private String answer;

    private String review;
}
