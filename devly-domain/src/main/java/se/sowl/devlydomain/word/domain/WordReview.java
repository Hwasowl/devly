package se.sowl.devlydomain.word.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.user.domain.User;

@Entity
@Table(name = "word_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordReview extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    private boolean correct;

    @Builder
    public WordReview(User user, Word word, Study study, boolean correct) {
        this.user = user;
        this.word = word;
        this.study = study;
        this.correct = correct;
    }

    public static WordReview of(User user, Word word, Study study, boolean correct) {
        return WordReview.builder()
            .user(user)
            .word(word)
            .study(study)
            .correct(correct)
            .build();
    }

    public void markAsCorrect() {
        if (!this.correct) {
            this.correct = true;
        }
    }
}
