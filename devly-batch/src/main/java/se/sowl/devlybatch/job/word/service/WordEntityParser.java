package se.sowl.devlybatch.job.word.service;

import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.gpt.GptEntityParser;
import se.sowl.devlybatch.common.gpt.GptRequestFactory;
import se.sowl.devlybatch.common.gpt.GptResponseValidator;
import se.sowl.devlybatch.common.gpt.exception.GPTContentProcessingException;
import se.sowl.devlydomain.word.domain.Word;

@Component
public class WordEntityParser extends GptEntityParser<Word> {
    public WordEntityParser(GptRequestFactory gptRequestFactory, GptResponseValidator gptResponseValidator) {
        super(gptRequestFactory, gptResponseValidator);
    }

    @Override
    protected Word parseEntity(Long studyId, String entry) {
        try {
            String[] lines = entry.split("\n");
            return Word.builder()
                .studyId(studyId)
                .word(lines[0].replace("단어: ", "").trim())
                .pronunciation(lines[1].replace("발음: ", "").trim())
                .meaning(lines[2].replace("의미: ", "").trim())
                .example(lines[3].replace("예문: ", "").trim())
                .quiz(lines[4].replace("퀴즈: ", "").trim())
                .build();
        } catch (Exception e) {
            throw new GPTContentProcessingException("Error parsing Word entity: " + e.getMessage(), e);
        }
    }
}
