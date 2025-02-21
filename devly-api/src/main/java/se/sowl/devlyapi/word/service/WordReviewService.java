package se.sowl.devlyapi.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.word.exception.AlreadyExistsReviewException;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlyapi.word.exception.ReviewNotFoundException;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;
import se.sowl.devlydomain.word.domain.WordReview;
import se.sowl.devlydomain.word.repository.WordReviewRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WordReviewService {
    public final WordReviewRepository wordReviewRepository;
    public final UserStudyRepository userStudyRepository;

    @Transactional
    public void createReview(Long studyId, Long userId, List<Long> correctIds, List<Long> incorrectIds) {
        if (wordReviewRepository.existsByStudyIdAndUserId(studyId, userId)) {
            throw new AlreadyExistsReviewException();
        }
        initialWordReviews(studyId, userId, correctIds, incorrectIds);
        updateUserStudyComplete(studyId, userId);
    }

    @Transactional
    public void updateReview(Long studyId, Long userId, List<Long> correctIds) {
        if (!wordReviewRepository.existsByStudyIdAndUserId(studyId, userId)) {
            throw new ReviewNotFoundException();
        }
        updateWordReviews(studyId, userId, correctIds);
        updateUserStudyComplete(studyId, userId);
    }

    private void updateWordReviews(Long studyId, Long userId, List<Long> correctIds) {
        if (correctIds.isEmpty()) {
            return;
        }
        wordReviewRepository.findAllByStudyIdAndUserId(studyId, userId).stream()
            .filter(review -> correctIds.contains(review.getWordId()))
            .forEach(WordReview::markAsCorrect);
    }

    private void updateUserStudyComplete(Long studyId, Long userId) {
        long count = wordReviewRepository.countByCorrectAndStudyIdAndUserId(true, studyId, userId);
        if (count == StudyTypeEnum.WORD.getRequiredCount()) {
            userStudyRepository.findByUserIdAndStudyId(userId, studyId)
                .ifPresentOrElse(UserStudy::complete, () -> {
                        throw new NotAssignmentWordStudyException();
                    }
                );
        }
    }

    private void initialWordReviews(Long studyId, Long userId, List<Long> correctIds, List<Long> incorrectIds) {
        List<WordReview> reviews = new ArrayList<>();
        reviews.addAll(correctIds.stream().map(id -> WordReview.of(userId, id, studyId, true)).toList());
        reviews.addAll(incorrectIds.stream().map(id -> WordReview.of(userId, id, studyId, false)).toList());
        wordReviewRepository.saveAll(reviews);
    }
}
