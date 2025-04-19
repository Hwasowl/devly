package se.sowl.devlydomain.word.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.user.domain.UserStudy;

@Entity
@Table(name = "word_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordReview extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_study_id", nullable = false)
    private UserStudy userStudy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(nullable = false)
    private boolean correct;

    @Builder
    public WordReview(UserStudy userStudy, Word word, boolean correct) {
        this.userStudy = userStudy;
        this.word = word;
        this.correct = correct;
    }

    public static WordReview of(UserStudy userStudy, Word word, boolean correct) {
        return WordReview.builder()
            .userStudy(userStudy)
            .word(word)
            .correct(correct)
            .build();
    }

    public void markAsCorrect() {
        if (!this.correct) {
            this.correct = true;
        }
    }
}
