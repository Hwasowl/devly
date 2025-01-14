package se.sowl.devlyapi.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.repository.WordRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;

    public WordListOfStudyResponse getList(Long studyId) {
        List<Word> allByStudy = wordRepository.findAllByStudyId(studyId);
        return WordListOfStudyResponse.from(allByStudy);
    }
}
