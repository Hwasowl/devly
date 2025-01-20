package se.sowl.devlybatch.job.word;

import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.util.ArrayList;
import java.util.List;

public class WordParser {

    public static List<Word> parseGPTResponse(GPTResponse response, Long studyId) {
        List<Word> words = new ArrayList<>();
        String content = response.getContent();
        String[] entries = content.split("---");
        for (String entry : entries) {
            if (entry.trim().isEmpty()) continue;
            add(studyId, entry, words);
        }
        return words;
    }

    private static void add(Long studyId, String entry, List<Word> words) {
        String[] lines = entry.trim().split("\n");
        words.add(Word.builder()
            .studyId(studyId).word(lines[0].replace("단어: ", "").trim())
            .pronunciation(lines[1].replace("발음: ", "").trim())
            .meaning(lines[2].replace("의미: ", "").trim())
            .example(lines[3].replace("예문: ", "").trim())
            .quiz(lines[4].replace("퀴즈: ", "").trim())
            .build());
    }
}
