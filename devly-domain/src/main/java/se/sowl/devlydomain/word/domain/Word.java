package se.sowl.devlydomain.word.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "words")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "study_id")
    private Long studyId;

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

    @Builder
    public Word(Long studyId, String word, String pronunciation, String meaning, String example, String quiz) {
        this.studyId = studyId;
        this.word = word;
        this.pronunciation = pronunciation;
        this.meaning = meaning;
        this.example = example;
        this.quiz = quiz;
    }
}
