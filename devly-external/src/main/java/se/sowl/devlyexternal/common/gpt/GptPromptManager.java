package se.sowl.devlyexternal.common.gpt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.sowl.devlyexternal.common.gpt.exception.PromptNotExistException;
import se.sowl.devlydomain.prompt.domain.GeneratePrompt;
import se.sowl.devlydomain.prompt.repository.PromptRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public abstract class GptPromptManager {
    private final PromptRepository promptRepository;

    public String getDefaultPrompt(Long developerTypeId, Long studyTypeId) {
        GeneratePrompt generatePrompt = promptRepository.findFirstByDeveloperTypeIdAndStudyTypeId(developerTypeId, studyTypeId)
            .orElseThrow(() -> new PromptNotExistException("프롬프트 정보를 찾을 수 없습니다."));
        return generatePrompt.getContent();
    }

    public void addExcludePrompt(List<String> excludeContents, StringBuilder prompt) {
        if(!excludeContents.isEmpty()) {
            prompt.append("\n다음 용어나 주제들은 제외해주세요:\n");
            excludeContents.forEach(word -> prompt.append("- ").append(word).append("\n"));
        }
    }

    public abstract void addBasePrompt(Long developerTypeId, Long studyTypeId, StringBuilder builder);
}
