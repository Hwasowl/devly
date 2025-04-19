package se.sowl.devlydomain.word.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.study.domain.Study;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "words")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Word extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String pronunciation;

    @Column(nullable = false)
    private String meaning;

    @Column(nullable = false, columnDefinition = "TEXT", length = 65535)
    private String example;

    @Column(nullable = false, columnDefinition = "TEXT", length = 65535)
    private String quiz;

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<WordReview> wordReviews = new ArrayList<>();

    @Builder
    public Word(Study study, String word, String pronunciation, String meaning, String example, String quiz) {
        this.study = study;
        this.word = word;
        this.pronunciation = pronunciation;
        this.meaning = meaning;
        this.example = example;
        this.quiz = quiz;
    }
}
