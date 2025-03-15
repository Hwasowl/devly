package se.sowl.devlybatch.job.word;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import se.sowl.devlybatch.config.TestBatchConfig;
import se.sowl.devlybatch.job.MediumBatchTest;
import se.sowl.devlydomain.prompt.domain.GeneratePrompt;
import se.sowl.devlydomain.prompt.repository.PromptRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatusEnum;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.repository.WordRepository;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;
import se.sowl.devlyexternal.client.gpt.exception.GPTClientException;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestBatchConfig.class, WordCreationJobConfig.class})
class WordCreationJobConfigTest extends MediumBatchTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private PromptRepository promptRepository;

    @MockBean
    private GPTClient gptClient;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
        jobLauncherTestUtils.setJob(wordCreationJob);
        jobLauncherTestUtils.setJobRepository(jobRepository);

        if (promptRepository.findFirstByDeveloperTypeIdAndStudyTypeId(1L, 1L).isEmpty()) {
            GeneratePrompt backendGeneratePrompt = new GeneratePrompt(1L, 1L, "백엔드 개발자를 위한 전문 용어를 생성해주세요.\n" +
                "단어: [영문 용어]\n" +
                "발음: [발음 기호]\n" +
                "의미: [한글 의미]\n" +
                "예문: {\"source\": \"공식 문서 출처\", \"text\": \"영문 예문\", \"translation\": \"한글 번역\"}\n" +
                "퀴즈: {\"text\": \"\", \"distractors\": [\"오답1\", \"오답2\", \"오답3\", \"오답4\"]}\n" +
                "---");
            promptRepository.save(backendGeneratePrompt);

            GeneratePrompt frontendGeneratePrompt = new GeneratePrompt(2L, 1L, "프론트엔드 개발자를 위한 전문 용어를 생성해주세요.\n" +
                "단어: [영문 용어]\n" +
                "발음: [발음 기호]\n" +
                "의미: [한글 의미]\n" +
                "예문: {\"source\": \"공식 문서 출처\", \"text\": \"영문 예문\", \"translation\": \"한글 번역\"}\n" +
                "퀴즈: {\"text\": \"\", \"distractors\": [\"오답1\", \"오답2\", \"오답3\", \"오답4\"]}\n" +
                "---");
            promptRepository.save(frontendGeneratePrompt);
        }
    }

    @AfterEach
    void tearDown() {
        wordRepository.deleteAll();
        studyRepository.deleteAll();
    }

    @Test
    @DisplayName("오늘 생성된 스터디에 대해 GPT 응답을 파싱하여 단어를 저장한다")
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
            .thenReturn(new GPTResponse(
                List.of(new GPTResponse.Choice(
                    new GPTResponse.Message("assistant", backendResponse),
                    "stop",
                    0
                )))
            )
            .thenReturn(new GPTResponse(
                List.of(new GPTResponse.Choice(
                    new GPTResponse.Message("assistant", frontendResponse),
                    "stop",
                    0
                )))
            );

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createWordsStep");

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        verify(gptClient, times(2)).generate(any());

        studyRepository.findAll().forEach(study -> {
            Assertions.assertThat(study.getStatus()).isEqualTo(StudyStatusEnum.CONNECTED);
        });

        List<Word> savedWords = wordRepository.findAll();
        assertThat(savedWords).hasSize(2);

        Word backendWord = wordRepository.findByStudyId(backendStudy.getId());
        assertThat(backendWord.getWord()).isEqualTo("implementation");
        assertThat(backendWord.getMeaning()).isEqualTo("구현, 실행");

        Word frontendWord = wordRepository.findByStudyId(frontendStudy.getId());
        assertThat(frontendWord.getWord()).isEqualTo("component");
        assertThat(frontendWord.getMeaning()).isEqualTo("구성 요소");
    }

    @Test
    @DisplayName("특정 스터디에 대한 GPT 응답 파싱 실패 시 예외가 발생하지만 배치는 성공한다.")
    void createWordsStepWithFailureTest() throws Exception {
        // given
        Study study = Study.builder()
            .typeId(1L)
            .developerTypeId(1L)
            .build();
        studyRepository.save(study);

        String invalidResponse = "Invalid Request";
        when(gptClient.generate(any())).thenThrow(new GPTClientException("GPT API error: " + invalidResponse));

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createWordsStep");

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        verify(gptClient, times(1)).generate(any());

        List<Word> savedWords = wordRepository.findAll();
        assertThat(savedWords).isEmpty();
    }
}
