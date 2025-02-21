package se.sowl.devlyapi.word.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.sowl.devlydomain.word.domain.WordReview;

import java.util.List;

@Getter
@AllArgsConstructor
public class WordReviewResponse {
    private List<Long> correctIds;
    private List<Long> incorrectIds;

    public static WordReviewResponse from(List<WordReview> reviews) {
        return new WordReviewResponse(
            reviews.stream().filter(WordReview::isCorrect).map(WordReview::getWordId).toList(),
            reviews.stream().filter(review -> !review.isCorrect()).map(WordReview::getWordId).toList()
        );
    }
}
