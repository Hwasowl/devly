package se.sowl.devlyapi.word.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.sowl.devlydomain.word.domain.Word;

import java.util.List;

@Getter
@AllArgsConstructor
public class WordListOfStudyResponse {
    private List<WordResponse> words;

    public static WordListOfStudyResponse from(List<Word> words) {
        return new WordListOfStudyResponse(words.stream().map(WordResponse::from).toList());
    }
}
