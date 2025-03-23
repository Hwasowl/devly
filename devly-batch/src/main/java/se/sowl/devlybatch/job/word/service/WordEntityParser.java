package se.sowl.devlybatch.job.word.service;

import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.JsonExtractor;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptEntityParser;
import se.sowl.devlyexternal.common.gpt.GptRequestFactory;
import se.sowl.devlyexternal.common.gpt.GptResponseValidator;
import se.sowl.devlyexternal.common.gpt.exception.GPTContentProcessingException;
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
    protected Word parseEntity(ParserArguments parameters, String entry) {
        Long studyId = parameters.get("studyId", Long.class);
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
