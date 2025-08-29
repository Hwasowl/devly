package se.sowl.devlybatch.job.word;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sowl.devlybatch.common.JsonExtractor;
import se.sowl.devlybatch.job.MediumBatchTest;
import se.sowl.devlybatch.job.word.service.WordEntityParser;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptRequestFactory;
import se.sowl.devlyexternal.common.gpt.GptResponseValidator;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class WordEntityParserTest extends MediumBatchTest {

    private final WordEntityParser wordEntityParser;

    @Autowired
    public WordEntityParserTest(StudyRepository studyRepository) {
        this.wordEntityParser = new WordEntityParser(
            new JsonExtractor(new ObjectMapper()),
            new GptRequestFactory(),
            new GptResponseValidator(),
            studyRepository
        );
    }

    @Test
    @DisplayName("GPT 응답을 파싱하여 Word 엔티티로 정확히 변환한다")
    void shouldParseGptResponseToWordEntities() {
        // given
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
        GPTResponse gptResponse = createGptResponse(responseContent);

        // when
        DeveloperType beType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
        StudyType studyType = studyTypeRepository.save(StudyType.builder().name("Word").build());
        Study study = studyRepository.save(buildStudy(studyType, beType));

        ParserArguments parseParameters = new ParserArguments().add("studyId", study.getId());
        List<Word> words = wordEntityParser.parseGPTResponse(gptResponse, parseParameters);

        // then
        // 첫 번째 단어 검증
        Word firstWord = words.get(0);
        assertThat(firstWord.getStudy().getId()).isEqualTo(study.getId());
        assertThat(firstWord.getWord()).isEqualTo("implementation");
        assertThat(firstWord.getPronunciation()).isEqualTo("/ˌɪmplɪmenˈteɪʃən/");
        assertThat(firstWord.getMeaning()).isEqualTo("구현, 실행");
        assertThat(firstWord.getExample()).contains("Spring Documentation");
        assertThat(firstWord.getQuiz()).contains("Interface");

        // 두 번째 단어 검증
        Word secondWord = words.get(1);
        assertThat(secondWord.getStudy().getId()).isEqualTo(study.getId());
        assertThat(secondWord.getWord()).isEqualTo("polymorphism");
        assertThat(secondWord.getPronunciation()).isEqualTo("/ˌpɒlɪˈmɔːfɪzəm/");
        assertThat(secondWord.getMeaning()).isEqualTo("다형성");
        assertThat(secondWord.getExample()).contains("Java Documentation");
        assertThat(secondWord.getQuiz()).contains("Inheritance");
    }
}
