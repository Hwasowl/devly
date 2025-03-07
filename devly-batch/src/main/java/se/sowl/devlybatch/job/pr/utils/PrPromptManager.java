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
        prompt.append("\n각 PR은 다음 형식으로 정확히 작성해주세요:\n");
        prompt.append("제목: [PR의 간결하고 명확한 제목]\n 설명: [PR에 대한 자세한 설명]\n");
        prompt.append("""
           변경 파일: [{"fileName": "파일 경로와 이름", "language": "프로그래밍 언어", "content": "파일 내용"}]
        """);
        prompt.append("""
            라벨: ["변경 파일에 해당하는 태그1", "변경 파일에 해당하는 태그2", "변경 파일에 해당하는 태그3"]\n
        """);
        prompt.append("""
            질문: ["변경 파일이나 PR 주제에서 제시할 수 있는 질문", "변경 파일이나 PR 주제에서 제시할 수 있는 질문"]\n
        """);
        prompt.append("중요: '변경 파일' 및 '라벨' 필드는 반드시 위와 같은 형식으로 작성해주세요. " +
            "특히 '변경 파일' 필드는 JSON 배열을 한 줄에 모두 작성해야 합니다." +
            "줄바꿈 없이 모든 JSON 내용이 한 줄에 있어야 합니다. 파일 내용의 줄바꿈은 \\n으로 표현해주세요.");
    }
}
