package se.sowl.devlybatch.job.word.service;

import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.gpt.GptContentProcessor;
import se.sowl.devlydomain.word.domain.Word;

import java.util.List;

@Component
public class WordContentProcessor extends GptContentProcessor<Word> {

    @Override
    protected void parseEntity(Long studyId, String entry, List<Word> words) {
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
