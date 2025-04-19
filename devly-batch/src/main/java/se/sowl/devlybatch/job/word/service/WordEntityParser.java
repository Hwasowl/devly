package se.sowl.devlybatch.job.word.service;

import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.JsonExtractor;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptEntityParser;
import se.sowl.devlyexternal.common.gpt.GptRequestFactory;
import se.sowl.devlyexternal.common.gpt.GptResponseValidator;
import se.sowl.devlyexternal.common.gpt.exception.GPTContentProcessingException;

@Component
public class WordEntityParser extends GptEntityParser<Word> {
    private final JsonExtractor jsonExtractor;
    private final StudyRepository studyRepository;

    public WordEntityParser(
        JsonExtractor jsonExtractor,
        GptRequestFactory gptRequestFactory,
        GptResponseValidator gptResponseValidator, StudyRepository studyRepository
    ) {
        super(gptRequestFactory, gptResponseValidator, studyRepository);
        this.jsonExtractor = jsonExtractor;
        this.studyRepository = studyRepository;
    }

    @Override
    protected Word parseEntity(ParserArguments parameters, String entry) {
        Long studyId = parameters.get("studyId", Long.class);
        Study study = studyRepository.findById(studyId).orElseThrow(() -> new IllegalArgumentException("Study not found with ID: " + studyId));
        try {
            return Word.builder()
                .study(study)
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
