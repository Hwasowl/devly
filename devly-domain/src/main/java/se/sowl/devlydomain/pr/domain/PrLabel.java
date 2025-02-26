package se.sowl.devlydomain.pr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "pr_labels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrLabel extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long prId;

    public PrLabel(Long prId) {
        this.prId = prId;
    }
}
