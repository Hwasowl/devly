package se.sowl.devlybatch.job.word.service;

import org.springframework.stereotype.Component;
import se.sowl.devlyexternal.common.gpt.GptPromptManager;
import se.sowl.devlydomain.prompt.repository.PromptRepository;

@Component
public class WordPromptManager extends GptPromptManager {
    public WordPromptManager(PromptRepository promptRepository) {
        super(promptRepository);
    }

    @Override
    public void addBasePrompt(Long developerTypeId, Long studyTypeId, StringBuilder builder) {
        String defaultPrompt = getDefaultPrompt(developerTypeId, studyTypeId);
        builder.append(defaultPrompt);
    }
}
