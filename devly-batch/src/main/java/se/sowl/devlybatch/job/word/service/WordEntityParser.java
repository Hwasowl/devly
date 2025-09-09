package se.sowl.devlybatch.job.word.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlyexternal.client.gpt.dto.WordGPTResponse;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptEntityParser;
import se.sowl.devlyexternal.common.gpt.GptRequestFactory;
import se.sowl.devlyexternal.common.gpt.GptResponseValidator;
import se.sowl.devlyexternal.common.gpt.exception.GPTContentProcessingException;

@Component
public class WordEntityParser extends GptEntityParser<Word> {
    private final ObjectMapper objectMapper;
    private final StudyRepository studyRepository;

    public WordEntityParser(
        ObjectMapper objectMapper,
        GptRequestFactory gptRequestFactory,
        GptResponseValidator gptResponseValidator,
        StudyRepository studyRepository) {
        super(gptRequestFactory, gptResponseValidator, objectMapper);
        this.objectMapper = objectMapper;
        this.studyRepository = studyRepository;
    }

    @Override
    protected Word parseEntity(ParserArguments parameters, String entry) {
        Long studyId = parameters.get("studyId", Long.class);
        Study study = studyRepository.findById(studyId).orElseThrow(() -> new IllegalArgumentException("Study not found with ID: " + studyId));
        try {
            WordGPTResponse wordResponse = objectMapper.readValue(entry, WordGPTResponse.class);
            return Word.builder()
                .study(study)
                .word(wordResponse.getWord())
                .pronunciation(wordResponse.getPronunciation())
                .meaning(wordResponse.getMeaning())
                .example(objectMapper.writeValueAsString(wordResponse.getExample()))
                .quiz(objectMapper.writeValueAsString(wordResponse.getQuiz()))
                .build();
        } catch (Exception e) {
            throw new GPTContentProcessingException("Error parsing Word entity: " + e.getMessage(), e);
        }
    }
}
