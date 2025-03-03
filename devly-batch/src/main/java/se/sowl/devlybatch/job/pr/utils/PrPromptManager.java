package se.sowl.devlybatch.job.pr.utils;

import org.springframework.stereotype.Component;
import se.sowl.devlybatch.common.gpt.GptPromptManager;

@Component
public class PrPromptManager extends GptPromptManager {
    @Override
    public void addPrompt(Long developerTypeId, StringBuilder prompt) {
        if (developerTypeId.equals(1L)) {
            prompt.append("백엔드 개발자를 위한 PR(Pull Request) 예시를 생성해주세요.");
        } else if (developerTypeId.equals(2L)) {
            prompt.append("프론트엔드 개발자를 위한 PR(Pull Request) 예시를 생성해주세요.");
        }
        prompt.append("\n각 PR은 다음 형식으로 작성해주세요:\n");
        prompt.append("제목: [PR의 간결하고 명확한 제목]\n");
        prompt.append("설명: [PR에 대한 자세한 설명]\n");
        prompt.append("변경 파일: [{\"fileName\": \"파일 경로와 이름\", \"language\": \"프로그래밍 언어\", \"content\": \"파일 내용\"}, {...}]\n");
        prompt.append("라벨: [\"Java\", \"Thread-safe\", \"Performance\"]\n---\n");
        prompt.append("변경 파일은 실제 코드의 일부로 2-3개의 파일을 포함해주세요. 라벨은 PR의 성격을 잘 나타내는 3-5개의 키워드를 포함해주세요.");
    }
}
