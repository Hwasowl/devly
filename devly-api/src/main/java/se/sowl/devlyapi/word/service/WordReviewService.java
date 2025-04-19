package se.sowl.devlyapi.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.study.service.StudyService;
import se.sowl.devlyapi.user.service.UserService;
import se.sowl.devlyapi.word.exception.AlreadyExistsReviewException;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlyapi.word.exception.ReviewNotFoundException;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.user.domain.User;
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
            .filter(review -> correctIds.contains(review.getWord().getId()))
            .forEach(WordReview::markAsCorrect);
    }

    private void updateUserStudyComplete(Long studyId, Long userId) {
        long count = wordReviewRepository.countByCorrectAndStudyIdAndUserId(true, studyId, userId);
        if (count == StudyTypeEnum.WORD.getRequiredCount()) {
            userStudyRepository.findByUserIdAndStudyId(userId, studyId)
                .ifPresentOrElse(UserStudy::complete, () -> {
                        throw new NotAssignmentWordStudyException(); // TODO: should make validate manager class? cannot assign exception in domain entity
                    }
                );
        }
    }

    private void initialWordReviews(Long studyId, Long userId, List<Long> correctIds, List<Long> incorrectIds) {
        Study study = studyService.getStudyById(studyId);
        User user = userService.getUserById(userId);
        List<Word> words = wordService.getList(study.getId(), user.getId());
        List<WordReview> reviews = getWordReviews(correctIds, incorrectIds, user, words, study);
        wordReviewRepository.saveAll(reviews);
    }

    private static List<WordReview> getWordReviews(List<Long> correctIds, List<Long> incorrectIds, User user, List<Word> words, Study study) {
        List<WordReview> reviews = new ArrayList<>();
        reviews.addAll(correctIds.stream()
            .map(id -> WordReview.of(user, words.stream().filter(word -> Objects.equals(word.getId(), id)).findFirst().orElseThrow(), study, true)).toList());
        reviews.addAll(incorrectIds.stream()
            .map(id -> WordReview.of(user, words.stream().filter(word -> Objects.equals(word.getId(), id)).findFirst().orElseThrow(), study, false)).toList());
        return reviews;
    }
}
