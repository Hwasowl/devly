package se.sowl.devlydomain.pr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "pr_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrComment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pull_request_id")
    private Long prId;

    private Long index;

    private String content;

    public PrComment(Long prId, Long index, String content) {
        this.prId = prId;
        this.index = index;
        this.content = content;
    }
}
