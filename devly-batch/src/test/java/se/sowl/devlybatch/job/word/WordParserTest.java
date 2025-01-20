package se.sowl.devlybatch.job.word;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class WordParserTest {

    @Test
    @DisplayName("단어 GPT 응답을 파싱 처리 해 Word 엔티티로 변환한다")
    void parseGPTResponseTest() {
        // given
        Long studyId = 1L;
        String responseContent = """
           단어: implementation
           발음: /ˌɪmplɪmenˈteɪʃən/
           의미: 구현, 실행
           예문: {"source": "Spring Documentation", "text": "The implementation details should be hidden.", "translation": "구현 세부사항은 숨겨져야 합니다."}
           퀴즈: {"text": "", "distractors": ["Interface", "Abstract", "Class", "Object"]}
           ---
           단어: polymorphism
           발음: /ˌpɒlɪˈmɔːfɪzəm/
           의미: 다형성
           예문: {"source": "Java Documentation", "text": "Polymorphism allows multiple implementations.", "translation": "다형성은 여러 구현을 허용합니다."}
           퀴즈: {"text": "", "distractors": ["Inheritance", "Encapsulation", "Abstraction", "Interface"]}
           ---
           """;
        GPTResponse gptResponse = new GPTResponse(
            List.of(new GPTResponse.Choice(
                new GPTResponse.Message("assistant", responseContent),
                "stop",
                0
            ))
        );

        // when
        List<Word> words = WordParser.parseGPTResponse(gptResponse, studyId);

        // then
        assertThat(words).hasSize(2);

        Word firstWord = words.getFirst();
        assertThat(firstWord.getStudyId()).isEqualTo(studyId);
        assertThat(firstWord.getWord()).isEqualTo("implementation");
        assertThat(firstWord.getPronunciation()).isEqualTo("/ˌɪmplɪmenˈteɪʃən/");
        assertThat(firstWord.getMeaning()).isEqualTo("구현, 실행");
        assertThat(firstWord.getExample()).contains("Spring Documentation");
        assertThat(firstWord.getQuiz()).contains("Interface");

        Word secondWord = words.get(1);
        assertThat(secondWord.getStudyId()).isEqualTo(studyId);
        assertThat(secondWord.getWord()).isEqualTo("polymorphism");
        assertThat(secondWord.getPronunciation()).isEqualTo("/ˌpɒlɪˈmɔːfɪzəm/");
        assertThat(secondWord.getMeaning()).isEqualTo("다형성");
        assertThat(secondWord.getExample()).contains("Java Documentation");
        assertThat(secondWord.getQuiz()).contains("Inheritance");
    }
}
