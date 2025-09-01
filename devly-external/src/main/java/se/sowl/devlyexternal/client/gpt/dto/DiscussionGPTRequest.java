package se.sowl.devlyexternal.client.gpt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiscussionGPTRequest {
    private final String topic;
    private final String category;
    private final String difficulty;
    private final Integer currentRound;
    private final Integer totalRounds;
    private final String currentQuestion;
    private final String userAnswer;

    // TODO: refactor
    public String buildPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 기술 면접관입니다. 다음 조건을 만족해야 합니다:\n");
        prompt.append("- 답변을 1-10점으로 평가\n");
        prompt.append("- 건설적인 피드백 제공\n");
        prompt.append("- 다음 질문은 이전 답변과 연관성 있게 구성\n");
        prompt.append("- 3라운드에서는 심화 질문 진행\n\n");
        
        prompt.append("현재 상황:\n");
        prompt.append("- 주제: ").append(topic).append("\n");
        prompt.append("- 카테고리: ").append(category).append("\n");
        prompt.append("- 난이도: ").append(difficulty).append("\n");
        prompt.append("- 라운드: ").append(currentRound).append("/").append(totalRounds).append("\n");
        prompt.append("- 질문: ").append(currentQuestion).append("\n");
        prompt.append("- 사용자 답변: ").append(userAnswer).append("\n\n");
        
        if (currentRound < totalRounds) {
            prompt.append("응답 형식 (JSON):\n");
            prompt.append("{\n");
            prompt.append("  \"feedback\": \"피드백 내용\",\n");
            prompt.append("  \"score\": 8.5,\n");
            prompt.append("  \"nextQuestion\": \"다음 질문\"\n");
            prompt.append("}");
        } else {
            prompt.append("응답 형식 (JSON) - 최종 라운드:\n");
            prompt.append("{\n");
            prompt.append("  \"feedback\": \"피드백 내용\",\n");
            prompt.append("  \"score\": 8.5,\n");
            prompt.append("  \"finalEvaluation\": {\n");
            prompt.append("    \"overallScore\": 8.2,\n");
            prompt.append("    \"strengths\": [\"강점1\", \"강점2\"],\n");
            prompt.append("    \"improvements\": [\"개선점1\", \"개선점2\"],\n");
            prompt.append("    \"summary\": \"전체 요약\"\n");
            prompt.append("  }\n");
            prompt.append("}");
        }
        
        return prompt.toString();
    }
}