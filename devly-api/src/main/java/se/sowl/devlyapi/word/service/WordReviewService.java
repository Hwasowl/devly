package se.sowl.devlyapi.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.study.service.StudyService;
import se.sowl.devlyapi.study.service.UserStudyService;
import se.sowl.devlyapi.user.service.UserService;
import se.sowl.devlyapi.word.exception.AlreadyExistsReviewException;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlyapi.word.exception.ReviewNotFoundException;
import se.sowl.devlydomain.study.domain.StudyTypeClassification;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.domain.WordReview;
import se.sowl.devlydomain.word.repository.WordReviewRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WordReviewService {
    public final WordReviewRepository wordReviewRepository;
    public final UserStudyRepository userStudyRepository;
    public final StudyService studyService;
    public final UserService userService;
    private final WordService wordService;
    private final UserStudyService userStudyService;

    @Transactional
    public void createReview(Long studyId, Long userId, List<Long> correctIds, List<Long> incorrectIds) {
        UserStudy userStudy = userStudyService.getUserStudy(userId, studyId);
        if (wordReviewRepository.existsByUserStudyId(userStudy.getId())) {
            throw new AlreadyExistsReviewException();
        }
        initialWordReviews(studyId, userId, correctIds, incorrectIds);
        updateUserStudyComplete(studyId, userId);
    }

    @Transactional
    public void updateReview(Long studyId, Long userId, List<Long> correctIds) {
        UserStudy userStudy = userStudyService.getUserStudy(userId, studyId);
        if (!wordReviewRepository.existsByUserStudyId(userStudy.getId())) {
            throw new ReviewNotFoundException();
        }
        updateWordReviews(studyId, userId, correctIds);
        updateUserStudyComplete(studyId, userId);
    }

    private void updateWordReviews(Long studyId, Long userId, List<Long> correctIds) {
        if (correctIds.isEmpty()) {
            return;
        }
        UserStudy userStudy = userStudyService.getUserStudy(userId, studyId);
        wordReviewRepository.findAllByUserStudyId(userStudy.getId()).stream()
            .filter(review -> correctIds.contains(review.getWord().getId()))
            .forEach(WordReview::markAsCorrect);
    }

    private void updateUserStudyComplete(Long studyId, Long userId) {
        UserStudy userStudy = userStudyService.getUserStudy(userId, studyId);
        long count = wordReviewRepository.countByCorrectAndUserStudyId(true, userStudy.getId());
        if (count == StudyTypeClassification.WORD.getRequiredCount()) {
            userStudyRepository.findByUserIdAndStudyId(userId, studyId)
                .ifPresentOrElse(UserStudy::complete, () -> {
                        throw new NotAssignmentWordStudyException(); // TODO: should make validate manager class? cannot assign exception in domain entity
                    }
                );
        }
    }

    private void initialWordReviews(Long studyId, Long userId, List<Long> correctIds, List<Long> incorrectIds) {
        UserStudy userStudy = userStudyRepository.findByUserIdAndStudyId(studyId, userId)
            .orElseThrow(() -> new IllegalArgumentException("UserStudy not found for userId: " + userId + " and studyId: " + studyId));
        List<Word> words = wordService.getList(userStudy.getUser().getId(), userStudy.getStudy().getId());
        List<WordReview> reviews = getWordReviews(correctIds, incorrectIds, userStudy, words);
        wordReviewRepository.saveAll(reviews);
    }

    private static List<WordReview> getWordReviews(List<Long> correctIds, List<Long> incorrectIds, UserStudy userStudy, List<Word> words) {
        List<WordReview> reviews = new ArrayList<>();
        reviews.addAll(correctIds.stream()
            .map(id -> WordReview.of(userStudy, words.stream().filter(word -> Objects.equals(word.getId(), id)).findFirst().orElseThrow(), true)).toList());
        reviews.addAll(incorrectIds.stream()
            .map(id -> WordReview.of(userStudy, words.stream().filter(word -> Objects.equals(word.getId(), id)).findFirst().orElseThrow(), false)).toList());
        return reviews;
    }
}
