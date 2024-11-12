package se.sowl.devlydomain.learningGroup.domain.word;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.learningGroup.domain.LearningGroup;

@Entity
@Table(name = "words")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Word extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private LearningGroup group;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(length = 100)
    private String pronunciation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Embedded
    private WordExample example;

    @Builder
    private Word(
        LearningGroup group,
        String word,
        String pronunciation,
        String meaning,
        WordExample example
    ) {
        this.group = group;
        this.word = word;
        this.pronunciation = pronunciation;
        this.meaning = meaning;
        this.example = example;
    }
}
