package se.sowl.devlydomain.pr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pr_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrComment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id")
    private Pr pr;

    private Long sequence;

    private String content;

    @OneToMany(mappedBy = "prComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrReview> reviews = new ArrayList<>();

    @Builder
    public PrComment(Long sequence, String content, Pr pr) {
        this.sequence = sequence;
        this.content = content;
        this.pr = pr;
    }
}
