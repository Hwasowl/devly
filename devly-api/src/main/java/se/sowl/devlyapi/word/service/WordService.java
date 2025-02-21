package se.sowl.devlyapi.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.dto.WordReviewResponse;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.domain.WordReview;
import se.sowl.devlydomain.word.repository.WordRepository;
import se.sowl.devlydomain.word.repository.WordReviewRepository;

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
        return WordReviewResponse.from(wordReviews);
    }
}
