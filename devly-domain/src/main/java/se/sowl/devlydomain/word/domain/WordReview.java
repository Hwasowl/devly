package se.sowl.devlydomain.word.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "word_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordReview extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "word_id")
    private Long wordId;

    @Column(name = "study_id")
    private Long studyId;

    private boolean correct;

    @Builder
    public WordReview(Long userId, Long wordId, Long studyId, boolean correct) {
        this.userId = userId;
        this.wordId = wordId;
        this.studyId = studyId;
        this.correct = correct;
    }
}
