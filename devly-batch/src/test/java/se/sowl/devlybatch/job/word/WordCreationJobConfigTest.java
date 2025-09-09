package se.sowl.devlybatch.job.word;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import se.sowl.devlybatch.config.TestBatchConfig;
import se.sowl.devlybatch.job.MediumBatchTest;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.prompt.domain.StudyPrompt;
import se.sowl.devlydomain.prompt.repository.StudyPromptRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatus;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.repository.WordRepository;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.exception.GPTClientException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private StudyPromptRepository promptRepository;

    @Autowired
    private Job wordCreationJob;

    @MockBean
    private GPTClient gptClient;

    @BeforeEach
    void setUp() {
        initializeJobLauncherTestUtils(wordCreationJob);
        initializePrompts();
        initializeStudyTypes();
    }

    @AfterEach
    void tearDown() {
        wordRepository.deleteAll();
        studyRepository.deleteAll();
        studyTypeRepository.deleteAll();
        developerTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("오늘 생성된 스터디에 대해 GPT 응답을 파싱하여 단어를 저장한다")
    void shouldCreateWordsFromGptResponse() throws Exception {
        // given
        StudyType studyType = findStudyTypeByName("Word");
        DeveloperType beType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
        Study backendStudy = studyRepository.save(buildStudy(studyType, beType));
        DeveloperType feType = developerTypeRepository.save(new DeveloperType("Frontend Developer"));
        Study frontendStudy = studyRepository.save(buildStudy(studyType, feType));

        String backendResponse = """
        {
          "word": "dependency injection",
          "pronunciation": "/dɪˈpendənsi ɪnˈdʒekʃən/",
          "meaning": "의존성 주입",
          "example": {
            "source": "Spring Framework Documentation",
            "text": "Dependency injection is a design pattern that allows objects to receive their dependencies from external sources.",
            "translation": "의존성 주입은 객체가 외부 소스로부터 의존성을 받을 수 있게 하는 설계 패턴입니다."
          },
          "quiz": {
            "text": "",
            "distractors": ["Constructor Injection", "Setter Injection", "Field Injection", "Method Injection"]
          }
        }
        """;

        String frontendResponse = """
        {
          "word": "virtual DOM",
          "pronunciation": "/ˈvɜːrtʃuəl diː oʊ ɛm/",
          "meaning": "가상 돔",
          "example": {
            "source": "React Official Documentation",
            "text": "The virtual DOM is a JavaScript representation of the actual DOM.",
            "translation": "가상 돔은 실제 돔의 자바스크립트 표현입니다."
          },
          "quiz": {
            "text": "",
            "distractors": ["Real DOM", "Shadow DOM", "Document Fragment", "HTML Element"]
          }
        }
        """;

        when(gptClient.generate(any()))
            .thenReturn(createGptResponse(backendResponse))
            .thenReturn(createGptResponse(frontendResponse));

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createWordsStep");

        // then
        assertJobExecutionCompleted(jobExecution);
        verify(gptClient, times(2)).generate(any());

        studyRepository.findAll().forEach(study -> {
            assertThat(study.getStatus()).isEqualTo(StudyStatus.CONNECTED);
        });

        List<Word> savedWords = wordRepository.findAll();
        assertThat(savedWords).hasSize(2);

        // 백엔드 단어 검증
        Word backendWord = wordRepository.findByStudyId(backendStudy.getId());
        assertThat(backendWord.getWord()).isEqualTo("dependency injection");
        assertThat(backendWord.getMeaning()).isEqualTo("의존성 주입");
        assertThat(backendWord.getPronunciation()).isEqualTo("/dɪˈpendənsi ɪnˈdʒekʃən/");

        // 프론트엔드 단어 검증
        Word frontendWord = wordRepository.findByStudyId(frontendStudy.getId());
        assertThat(frontendWord.getWord()).isEqualTo("virtual DOM");
        assertThat(frontendWord.getMeaning()).isEqualTo("가상 돔");
        assertThat(frontendWord.getPronunciation()).isEqualTo("/ˈvɜːrtʃuəl diː oʊ ɛm/");
    }

    @Test
    @DisplayName("5번 호출 중 3번째 호출에 실패하고 나머지는 모두 성공하면 4개의 단어가 저장되어야 한다")
    void shouldCreateFourWordsWhenOneRequestFails() throws Exception {
        // given
        StudyType studyType = findStudyTypeByName("Word");
        DeveloperType beType = developerTypeRepository.save(new DeveloperType("backend"));
        List<Study> studies = IntStream.range(0, 5)
            .mapToObj(i -> Study.builder()
                .studyType(studyType)
                .developerType(beType)
                .build())
            .collect(Collectors.toList());
        studyRepository.saveAll(studies);

        String validResponse = """
        {
          "word": "microservice",
          "pronunciation": "/ˈmaɪkroʊsɜːrvɪs/",
          "meaning": "마이크로서비스",
          "example": {
            "source": "Spring Boot Documentation",
            "text": "A microservice is a small, independent service that can be deployed separately.",
            "translation": "마이크로서비스는 독립적으로 배포할 수 있는 작고 독립적인 서비스입니다."
          },
          "quiz": {
            "text": "",
            "distractors": ["Monolith", "Serverless", "Container", "API Gateway"]
          }
        }
        """;

        when(gptClient.generate(any()))
            .thenReturn(createGptResponse(validResponse))
            .thenReturn(createGptResponse(validResponse))
            .thenThrow(new GPTClientException("GPT API error"))
            .thenReturn(createGptResponse(validResponse))
            .thenReturn(createGptResponse(validResponse));

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createWordsStep");

        // then
        assertJobExecutionCompleted(jobExecution);
        verify(gptClient, times(5)).generate(any());

        List<Word> savedWords = wordRepository.findAll();
        assertThat(savedWords).hasSize(4);

        List<Study> updatedStudies = studyRepository.findAll();
        long connectedCount = updatedStudies.stream()
            .filter(s -> s.getStatus() == StudyStatus.CONNECTED)
            .count();
        assertThat(connectedCount).isEqualTo(4);
    }

    @Test
    @DisplayName("특정 스터디에 대한 GPT 응답 파싱 실패 시 예외가 발생하지만 배치는 성공한다")
    void shouldCompleteJobWhenParsingFails() throws Exception {
        // given
        StudyType studyType = findStudyTypeByName("Word");
        DeveloperType beType = developerTypeRepository.save(new DeveloperType("backend"));
        studyRepository.save(Study.builder().studyType(studyType).developerType(beType).build());

        String invalidResponse = "Invalid Request";
        when(gptClient.generate(any())).thenThrow(new GPTClientException("GPT API error: " + invalidResponse));

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createWordsStep");

        // then
        assertJobExecutionCompleted(jobExecution);
        verify(gptClient, times(1)).generate(any());

        List<Word> savedWords = wordRepository.findAll();
        assertThat(savedWords).isEmpty();
    }

    private void initializePrompts() {
        if (promptRepository.findByDeveloperTypeIdAndStudyTypeId(1L, 1L).isEmpty()) {
            StudyPrompt backendWordPrompt = new StudyPrompt(1L, 1L,
                    "You are an expert in technical terminology education for backend developers.\n" +
                            "Generate technical terms for backend developers.\n\n" +
                            "Please respond with a JSON object in the following format:\n" +
                            "{\n" +
                            "  \"word\": \"[English term]\",\n" +
                            "  \"pronunciation\": \"[pronunciation guide]\",\n" +
                            "  \"meaning\": \"[Korean meaning]\",\n" +
                            "  \"example\": {\n" +
                            "    \"source\": \"[official documentation source]\",\n" +
                            "    \"text\": \"[English example sentence]\",\n" +
                            "    \"translation\": \"[Korean translation]\"\n" +
                            "  },\n" +
                            "  \"quiz\": {\n" +
                            "    \"text\": \"\",\n" +
                            "    \"distractors\": [\"wrong1\", \"wrong2\", \"wrong3\", \"wrong4\"]\n" +
                            "  }\n" +
                            "}");
            promptRepository.save(backendWordPrompt);

            StudyPrompt frontendWordPrompt = new StudyPrompt(2L, 1L,
                    "You are an expert in technical terminology education for frontend developers.\n" +
                            "Generate technical terms for frontend developers.\n\n" +
                            "Please respond with a JSON object in the following format:\n" +
                            "{\n" +
                            "  \"word\": \"[English term]\",\n" +
                            "  \"pronunciation\": \"[pronunciation guide]\",\n" +
                            "  \"meaning\": \"[Korean meaning]\",\n" +
                            "  \"example\": {\n" +
                            "    \"source\": \"[official documentation source]\",\n" +
                            "    \"text\": \"[English example sentence]\",\n" +
                            "    \"translation\": \"[Korean translation]\"\n" +
                            "  },\n" +
                            "  \"quiz\": {\n" +
                            "    \"text\": \"\",\n" +
                            "    \"distractors\": [\"wrong1\", \"wrong2\", \"wrong3\", \"wrong4\"]\n" +
                            "  }\n" +
                            "}");
            promptRepository.save(frontendWordPrompt);
        }
    }
}
