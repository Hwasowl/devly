package se.sowl.devlydomain.level.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "levels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Level extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer level;

    @Column(name = "required_exp")
    private Integer requiredExp;

    @Builder
    public Level(Integer level, Integer requiredExp) {
        this.level = level;
        this.requiredExp = requiredExp;
    }

    public boolean canPromote(Integer currentExp) {
        return currentExp >= this.requiredExp;
    }
}
