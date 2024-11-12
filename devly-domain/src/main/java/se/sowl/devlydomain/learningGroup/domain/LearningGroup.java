package se.sowl.devlydomain.learningGroup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.learningGroup.domain.devKnowledge.DevKnowledge;
import se.sowl.devlydomain.learningGroup.domain.word.Word;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "learning_groups",
    uniqueConstraints = @UniqueConstraint(name = "uk_type_sequence", columnNames = {"type", "sequence"}),
    indexes = @Index(name = "idx_active_type", columnList = "active, type")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LearningType type;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false)
    private Integer difficulty;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Boolean active = true;

    @OneToMany(mappedBy = "group")
    private List<Word> words = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<DevKnowledge> devKnowledge = new ArrayList<>();

    @Builder
    private LearningGroup(
        LearningType type,
        Integer sequence,
        Integer difficulty,
        String title,
        String description
    ) {
        validateDifficulty(difficulty);
        this.type = type;
        this.sequence = sequence;
        this.difficulty = difficulty;
        this.title = title;
        this.description = description;
    }

    private void validateDifficulty(Integer difficulty) {
        if (difficulty < 1 || difficulty > 5) {
            throw new IllegalArgumentException("난이도는 1-5 사이여야 합니다.");
        }
    }

    public void update(
        Integer difficulty,
        String title,
        String description
    ) {
        validateDifficulty(difficulty);
        this.difficulty = difficulty;
        this.title = title;
        this.description = description;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
