package se.sowl.devlyapi.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.study.service.UserStudyService;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.dto.reviews.WordReviewResponse;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.domain.WordReview;
import se.sowl.devlydomain.word.repository.WordRepository;
import se.sowl.devlydomain.word.repository.WordReviewRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;
    private final WordReviewRepository wordReviewRepository;
    private final UserStudyService userStudyService;

    public List<Word> getList(Long userId, Long studyId) {
        userStudyService.validateUserStudyExist(userId, studyId);
        return wordRepository.findAllByStudyId(studyId);
    }

    public WordListOfStudyResponse getListResponse(Long userId, Long studyId) {
        userStudyService.validateUserStudyExist(userId, studyId);
        List<Word> allByStudy = wordRepository.findAllByStudyId(studyId);
        return WordListOfStudyResponse.from(allByStudy);
    }

    public WordReviewResponse getWordReviewsResponse(Long studyId, Long userId) {
        UserStudy userStudy = userStudyService.getUserStudy(userId, studyId);
        List<WordReview> wordReviews = wordReviewRepository.findAllByUserStudyId(userStudy.getId());
        return WordReviewResponse.from(wordReviews);
    }
}
