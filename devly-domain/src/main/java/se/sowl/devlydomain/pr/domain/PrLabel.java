package se.sowl.devlydomain.pr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "pr_labels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrLabel extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pr_id")
    @JsonIgnore
    private Pr pr;

    private String label;

    @Builder
    public PrLabel(Pr pr, String label) {
        this.pr = pr;
        this.label = label;
    }
}
