package se.sowl.devlybatch.job.word;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import se.sowl.devlybatch.config.TestBatchConfig;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.repository.WordRepository;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestBatchConfig.class, WordCreationJobConfig.class})
@Sql(scripts = {
    "/org/springframework/batch/core/schema-drop-h2.sql",
    "/org/springframework/batch/core/schema-h2.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class WordCreationJobConfigTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private WordRepository wordRepository;

    @MockBean
    private GPTClient gptClient;

    @AfterEach
    void tearDown() {
        wordRepository.deleteAll();
        studyRepository.deleteAll();
    }

    @Test
    @DisplayName("스텝 실행시 오늘 생성된 스터디에 대해 GPT 응답을 파싱하여 단어를 저장한다")
    void createWordsStepTest() throws Exception {
        // given
        Study backendStudy = Study.builder()
            .typeId(1L)
            .developerTypeId(1L)
            .build();
        Study frontendStudy = Study.builder()
            .typeId(1L)
            .developerTypeId(2L)
            .build();
        studyRepository.saveAll(List.of(backendStudy, frontendStudy));

        String backendResponse = """
        단어: implementation
        발음: /ˌɪmplɪmenˈteɪʃən/
        의미: 구현, 실행
        예문: {"source": "Spring Documentation", "text": "The implementation details should be hidden.", "translation": "구현 세부사항은 숨겨져야 합니다."}
        퀴즈: {"text": "", "distractors": ["Interface", "Abstract", "Class", "Object"]}
        ---
        """;

        String frontendResponse = """
        단어: component
        발음: /kəmˈpəʊnənt/
        의미: 구성 요소
        예문: {"source": "React Documentation", "text": "Components let you split the UI into independent pieces.", "translation": "컴포넌트를 사용하면 UI를 독립적인 부분으로 분할할 수 있습니다."}
        퀴즈: {"text": "", "distractors": ["Module", "Package", "Library", "Framework"]}
        ---
        """;

        when(gptClient.generate(any()))
            .thenReturn(new GPTResponse(backendResponse))
            .thenReturn(new GPTResponse(frontendResponse));

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createWordsStep");

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        verify(gptClient, times(2)).generate(any());

        List<Word> savedWords = wordRepository.findAll();
        assertThat(savedWords).hasSize(2);

        Word backendWord = wordRepository.findByStudyId(backendStudy.getId());
        assertThat(backendWord.getWord()).isEqualTo("implementation");
        assertThat(backendWord.getMeaning()).isEqualTo("구현, 실행");

        Word frontendWord = wordRepository.findByStudyId(frontendStudy.getId());
        assertThat(frontendWord.getWord()).isEqualTo("component");
        assertThat(frontendWord.getMeaning()).isEqualTo("구성 요소");
    }
}
