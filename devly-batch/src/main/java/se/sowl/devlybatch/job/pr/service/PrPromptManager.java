package se.sowl.devlybatch.job.pr.service;

import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.gpt.GptPromptManager;
import se.sowl.devlydomain.prompt.repository.PromptRepository;

@Component
public class PrPromptManager extends GptPromptManager {
    public PrPromptManager(PromptRepository promptRepository) {
        super(promptRepository);
    }

    @Override
    public void addBasePrompt(Long developerTypeId, Long studyTypeId, StringBuilder builder) {
        String defaultPrompt = getDefaultPrompt(developerTypeId, studyTypeId);
        builder.append(defaultPrompt);
    }
}
