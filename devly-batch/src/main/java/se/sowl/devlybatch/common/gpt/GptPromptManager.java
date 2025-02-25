package se.sowl.devlybatch.common.gpt;

import java.util.List;

public abstract class GptPromptManager {
    abstract protected void addPrompt(Long developerTypeId, StringBuilder prompt);

    public void addExcludePrompt(List<String> excludeContents, StringBuilder prompt) {
        if(!excludeContents.isEmpty()) {
            prompt.append("\n다음 용어들은 제외해주세요:\n");
            excludeContents.forEach(word -> prompt.append("- ").append(word).append("\n"));
        }
    }
}
