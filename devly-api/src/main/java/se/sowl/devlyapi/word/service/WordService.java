package se.sowl.devlyapi.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.repository.WordRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;
    private final UserStudyRepository userStudyRepository;

    public WordListOfStudyResponse getList(Long userId, Long studyId) {
        Optional<UserStudy> optionalUserStudy = userStudyRepository.findByUserIdAndStudyId(userId, studyId);
        if (optionalUserStudy.isEmpty()) {
            throw new NotAssignmentWordStudyException();
        }
        List<Word> allByStudy = wordRepository.findAllByStudyId(studyId);
        return WordListOfStudyResponse.from(allByStudy);
    }
}
