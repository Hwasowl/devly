package se.sowl.devlyapi.word.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.repository.WordRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;

    public List<Word> getList(Long studyId) {
        return wordRepository.getAllByStudyId(studyId);
    }
}
