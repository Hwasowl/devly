package se.sowl.devlybatch.job.word;

import java.util.List;

public class WordPromptManager {

    public static void addDefaultPrompt(Long developerTypeId, StringBuilder prompt) {
        if (developerTypeId.equals(1L)) {
            prompt.append("백엔드에 사용되는 언어들 중 공식 문서를 기반으로 백엔드 개발자를 위한 전문 용어 5개를 생성해주세요." +
                "문서에서 사용되는 단어가 다른 형식으로 사용된다면 유연하게 처리해주세요. dependency -> dependencies 로 사용 된 경우 곤란해집니다." +
                "개발 용어가 아닌(빈, AOP, 스프링 부트) 영어 단어여야 하며, 문서에서 사용되는 각 용어는 다음 형식으로 작성하고" +
                "의미를 정확하게 이해할 수 있도록 묘사해주세요. 또한 translation 값도 정확하게 묘사해주세요." +
                "quiz는 단어를 맞추는 형식으로 생성한 단어가 들어간 문장을 만들어주세요. 빈칸 처리는 프론트에서 처리합니다." +
                "용어 구분은 ---를 사용하세요:\n\n");
        } else if (developerTypeId.equals(2L)) {
            prompt.append("프론트엔드에 사용되는 언어들 중 공식 문서를 기반으로 프론트엔드 개발자를 위한 전문 용어 5개를 생성해주세요." +
                "문서에서 사용되는 단어가 다른 형식으로 사용된다면 유연하게 처리해주세요. dependency -> dependencies 로 사용 된 경우 곤란해집니다." +
                "개발 용어가 아닌(컴포넌트, 타입스크립트, 리엑트) 영어 단어여야 하며, 각 용어는 다음 형식으로 작성하고 " +
                "의미를 정확하게 이해할 수 있도록 묘사해주세요. 또한 translation 값도 정확하게 묘사해주세요." +
                "quiz는 단어를 맞추는 형식으로 생성한 단어가 들어간 문장을 만들어주세요. 빈칸 처리는 프론트에서 처리합니다." +
                "용어 구분은 ---를 사용하세요:\n\n");
        }
        prompt.append("단어: [영문 용어]\n발음: [발음 기호]\n의미: [한글 의미]\n예문: {\"source\": \"공식 문서 출처\", \"text\": \"영문 예문\", \"translation\": \"한글 번역\"}\n퀴즈: {\"text\": \"\", \"distractors\": [\"오답1\", \"오답2\", \"오답3\", \"오답4\"]}\n---\n");
    }

    public static void addExcludePrompt(List<String> excludeWords, StringBuilder prompt) {
        if(!excludeWords.isEmpty()) {
            prompt.append("\n다음 단어들은 제외해주세요:\n");
            excludeWords.forEach(word -> prompt.append("- ").append(word).append("\n"));
        }
    }
}
