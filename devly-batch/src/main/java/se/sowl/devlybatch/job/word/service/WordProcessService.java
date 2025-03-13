package se.sowl.devlybatch.job.word.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlybatch.job.word.exception.EmptyWordsException;
import se.sowl.devlybatch.job.word.exception.WordCreationException;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.repository.WordRepository;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordProcessService {
    private final WordRepository wordRepository;
    private final GPTClient gptClient;
    private final WordContentProcessor wordContentProcessor;
    private final WordPromptManager wordPromptManager;


    @Transactional
    public Long createWordsForStudy(Study study) {
        try {
            String prompt = createWordGeneratePrompt(study);
            List<Word> words = createWordsFromGpt(study, prompt);
            wordRepository.saveAll(words);
            return study.getId();
        } catch (Exception e) {
            log.error("Error while creating words for study {}", study.getId(), e);
            throw new WordCreationException("Failed to create words for study: " + study.getId(), e);
        }
    }

    private List<Word> createWordsFromGpt(Study study, String prompt) {
        GPTResponse response = gptClient.generate(wordContentProcessor.createGPTRequest(prompt));
        List<Word> words = wordContentProcessor.parseGPTResponse(response, study.getId());
        if (words.isEmpty()) {
            throw new EmptyWordsException("No words parsed for study: " + study.getId());
        }
        return words;
    }

    private String createWordGeneratePrompt(Study study) {
        List<String> recentWords = wordRepository.findWordsByCreatedAtAfter(LocalDateTime.now().minusDays(7))
            .stream().map(Word::getWord).collect(Collectors.toList());

        StringBuilder prompt = new StringBuilder();
        wordPromptManager.addPrompt(study.getDeveloperTypeId(), prompt);
        wordPromptManager.addExcludePrompt(recentWords, prompt);
        return prompt.toString();
    }
}
