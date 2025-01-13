package se.sowl.devlyapi.word.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.sowl.devlydomain.word.domain.Word;


@Getter
@AllArgsConstructor
public class WordResponse {
    private Long id;
    private String word;
    private String meaning;
    private String example;
    private String pronunciation;

    public static WordResponse from(Word word) {
        return new WordResponse(word.getId(), word.getWord(), word.getMeaning(), word.getExample(), word.getPronunciation());
    }
}
