package se.sowl.devlybatch.job.word.service;

import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.JsonExtractor;
import se.sowl.devlybatch.common.gpt.GptEntityParser;
import se.sowl.devlybatch.common.gpt.GptRequestFactory;
import se.sowl.devlybatch.common.gpt.GptResponseValidator;
import se.sowl.devlybatch.common.gpt.exception.GPTContentProcessingException;
import se.sowl.devlydomain.word.domain.Word;

@Component
public class WordEntityParser extends GptEntityParser<Word> {
    private final JsonExtractor jsonExtractor;

    public WordEntityParser(
        JsonExtractor jsonExtractor,
        GptRequestFactory gptRequestFactory,
        GptResponseValidator gptResponseValidator
    ) {
        super(gptRequestFactory, gptResponseValidator);
        this.jsonExtractor = jsonExtractor;
    }

    @Override
    protected Word parseEntity(Long studyId, String entry) {
        try {
            return Word.builder()
                .studyId(studyId)
                .word(jsonExtractor.extractField(entry, "단어:"))
                .pronunciation(jsonExtractor.extractField(entry, "발음:"))
                .meaning(jsonExtractor.extractField(entry, "의미:"))
                .example(jsonExtractor.extractField(entry, "예문:"))
                .quiz(jsonExtractor.extractField(entry, "퀴즈:"))
                .build();
        } catch (Exception e) {
            throw new GPTContentProcessingException("Error parsing Word entity: " + e.getMessage(), e);
        }
    }
}
