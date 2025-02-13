package se.sowl.devlyapi.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.study.dto.WordReviewResponse;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.domain.WordReview;
import se.sowl.devlydomain.word.repository.WordRepository;
import se.sowl.devlydomain.word.repository.WordReviewRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;
    private final WordReviewRepository wordReviewRepository;
    private final UserStudyRepository userStudyRepository;

    public WordListOfStudyResponse getList(Long userId, Long studyId) {
        Optional<UserStudy> optionalUserStudy = userStudyRepository.findByUserIdAndStudyId(userId, studyId);
        if (optionalUserStudy.isEmpty()) {
            throw new NotAssignmentWordStudyException();
        }
        List<Word> allByStudy = wordRepository.findAllByStudyId(studyId);
        return WordListOfStudyResponse.from(allByStudy);
    }

    public WordReviewResponse getWordReviews(Long studyId, Long userId) {
        List<WordReview> wordReviews = wordReviewRepository.findAllByStudyIdAndUserId(studyId, userId);
        List<Long> correctIds = wordReviews.stream().filter(WordReview::isCorrect).map(WordReview::getWordId).toList();
        List<Long> incorrectIds = wordReviews.stream().filter(review -> !review.isCorrect()).map(WordReview::getWordId).toList();
        return new WordReviewResponse(correctIds, incorrectIds);
    }

    @Transactional
    public void review (Long studyId, Long userId, List<Long> correctIds, List<Long> incorrectIds) {
        boolean exists = wordReviewRepository.existsByStudyIdAndUserId(studyId, userId);
        if (!exists) {
            initialWordReviews(studyId, userId, correctIds, incorrectIds);
        } else {
            updateWordReviews(studyId, userId, incorrectIds);
        }
        updateUserStudyComplete(studyId, userId);
    }

    private void updateWordReviews(Long studyId, Long userId, List<Long> incorrectIds) {
        if (incorrectIds.isEmpty()) {
            return;
        }
        wordReviewRepository.findAllByStudyIdAndUserId(studyId, userId).stream()
            .filter(review -> !incorrectIds.contains(review.getWordId()))
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
