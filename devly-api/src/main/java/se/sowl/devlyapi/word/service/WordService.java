package se.sowl.devlyapi.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
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

    public void review (Long studyId, Long userId, List<Long> correctIds, List<Long> incorrectIds) {
        List<WordReview> reviews = new ArrayList<>();
        reviews.addAll(correctIds.stream()
            .map(id -> WordReview.builder().userId(userId).studyId(studyId).wordId(id).correct(true).build()).toList());
        reviews.addAll(incorrectIds.stream()
            .map(id -> WordReview.builder().userId(userId).studyId(studyId).wordId(id).correct(false).build()).toList());
        wordReviewRepository.saveAll(reviews);
    }
}
