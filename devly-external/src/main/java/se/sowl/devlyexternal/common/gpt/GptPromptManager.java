package se.sowl.devlyexternal.common.gpt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.sowl.devlydomain.prompt.domain.GeneratePrompt;
import se.sowl.devlydomain.prompt.domain.RolePrompt;
import se.sowl.devlydomain.prompt.repository.GeneratePromptRepository;
import se.sowl.devlydomain.prompt.repository.RolePromptRepository;
import se.sowl.devlyexternal.common.gpt.exception.PromptNotExistException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GptPromptManager {
    private final GeneratePromptRepository generatePromptRepository;
    private final RolePromptRepository rolePromptRepository;

    public void addBasePrompt(Long developerTypeId, Long studyTypeId, StringBuilder builder) {
        GeneratePrompt generatePrompt = generatePromptRepository.findFirstByDeveloperTypeIdAndStudyTypeId(developerTypeId, studyTypeId)
            .orElseThrow(() -> new PromptNotExistException("생성 프롬프트 정보를 찾을 수 없습니다."));
        builder.append(generatePrompt.getContent());
    }

    public void addRolePrompt(Long developerTypeId, Long studyTypeId, StringBuilder builder) {
        RolePrompt rolePrompt = rolePromptRepository.findFirstByDeveloperTypeIdAndStudyTypeId(developerTypeId, studyTypeId)
            .orElseThrow(() -> new PromptNotExistException("역할 프롬프트 정보를 찾을 수 없습니다."));
        builder.append(rolePrompt.getContent());
    }

    public void addExcludePrompt(List<String> excludeContents, StringBuilder prompt) {
        if (!excludeContents.isEmpty()) {
            prompt.append("\n다음 용어나 주제들은 제외 해주세요:\n");
            excludeContents.forEach(word -> prompt.append("- ").append(word).append("\n"));
        }
    }
}
