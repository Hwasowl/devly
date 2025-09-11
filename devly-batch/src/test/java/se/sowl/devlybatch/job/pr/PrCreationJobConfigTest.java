package se.sowl.devlybatch.job.pr;

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
import org.springframework.test.context.jdbc.Sql;
import se.sowl.devlybatch.config.TestBatchConfig;
import se.sowl.devlybatch.job.MediumBatchTest;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.pr.repository.PrChangedFileRepository;
import se.sowl.devlydomain.pr.repository.PrLabelRepository;
import se.sowl.devlydomain.pr.repository.PrRepository;
import se.sowl.devlydomain.prompt.domain.StudyPrompt;
import se.sowl.devlydomain.prompt.repository.StudyPromptRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatus;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlyexternal.client.gpt.GPTClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestBatchConfig.class, PrCreationJobConfig.class})
@Sql(scripts = {
    "/org/springframework/batch/core/schema-drop-h2.sql",
    "/org/springframework/batch/core/schema-h2.sql"
}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class PrCreationJobConfigTest extends MediumBatchTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private PrRepository prRepository;

    @Autowired
    private PrChangedFileRepository prChangedFileRepository;

    @Autowired
    private PrLabelRepository prLabelRepository;

    @Autowired
    private StudyPromptRepository promptRepository;

    @Autowired
    private Job prCreationJob;

    @MockBean
    private GPTClient gptClient;

    @BeforeEach
    void setUp() {
        initializeJobLauncherTestUtils(prCreationJob);
        initializePrompts();
        initializeStudyTypes();
    }

    @AfterEach
    void tearDown() {
        prLabelRepository.deleteAll();
        prChangedFileRepository.deleteAll();
        prRepository.deleteAll();
        studyRepository.deleteAll();
    }

    @Test
    @DisplayName("오늘 생성된 스터디에 대해 GPT 응답을 파싱하여 PR을 저장한다")
    void shouldCreatePrsFromGptResponse() throws Exception {
        // given
        StudyType prStudyType = findStudyTypeByName("PR");
        DeveloperType backendType = developerTypeRepository.save(DeveloperType.builder().name("Backend").build());
        DeveloperType frontendType = developerTypeRepository.save(DeveloperType.builder().name("Frontend").build());

        Study backendStudy = buildStudy(prStudyType, backendType);
        Study frontendStudy = buildStudy(prStudyType, frontendType);
        studyRepository.saveAll(List.of(backendStudy, frontendStudy));

        // GPT 응답 준비
        String backendPrResponse = createBackendPrResponseContent();
        String frontendPrResponse = createFrontendPrResponseContent();

        when(gptClient.generate(any()))
            .thenReturn(createGptResponse(backendPrResponse))
            .thenReturn(createGptResponse(frontendPrResponse));

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createPrStep");

        // then
        assertJobExecutionCompleted(jobExecution);
        verify(gptClient, times(2)).generate(any());

        studyRepository.findAll().forEach(study -> {
            assertThat(study.getStatus()).isEqualTo(StudyStatus.CONNECTED)
                .withFailMessage("스터디 상태가 CONNECTED로 변경되어야 합니다");
        });

        List<Pr> savedPrs = prRepository.findAll();
        assertThat(savedPrs).hasSize(2)
            .withFailMessage("2개의 PR이 생성되어야 합니다");

        verifyBackendPr(savedPrs);
        verifyFrontendPr(savedPrs);
    }

    private String createBackendPrResponseContent() {
        return """
        {
          "title": "싱글톤 패턴 구현 개선",
          "description": "Thread-safe한 싱글톤 패턴으로 개선하고, 불필요한 메모리 사용을 줄였습니다.",
          "changedFiles": [
            {
              "fileName": "src/main/java/com/example/SingletonService.java",
              "language": "Java",
              "content": "public class SingletonService {\\n\\n    private static volatile SingletonService instance;\\n\\n    private SingletonService() {\\n        // private constructor\\n    }\\n\\n    public static SingletonService getInstance() {\\n        if (instance == null) {\\n            synchronized (SingletonService.class) {\\n                if (instance == null) {\\n                    instance = new SingletonService();\\n                }\\n            }\\n        }\\n        return instance;\\n    }\\n}"
            }
          ],
          "labels": ["Java", "Thread-safe", "Singleton"],
          "reviewComments": [
            "이 싱글톤 패턴에서 double-checked locking의 성능 이점은 무엇인가요?",
            "volatile 키워드를 사용한 이유와 메모리 가시성에 대해 설명해주세요."
          ]
        }
        """;
    }

    private String createFrontendPrResponseContent() {
        return """
        {
          "title": "리액트 컴포넌트 최적화",
          "description": "불필요한 렌더링을 방지하기 위해 React.memo와 useCallback 훅을 적용했습니다.",
          "changedFiles": [
            {
              "fileName": "src/components/ProductList.jsx",
              "language": "JavaScript",
              "content": "import React, { useCallback } from 'react';\\n\\nconst ProductItem = React.memo(({ product, onSelect }) => {\\n  return (\\n    <div className=\\"product-item\\" onClick={() => onSelect(product.id)}>\\n      <h3>{product.name}</h3>\\n      <p>{product.price}</p>\\n    </div>\\n  );\\n});\\n\\nconst ProductList = ({ products, onSelectProduct }) => {\\n  const handleSelect = useCallback((id) => {\\n    onSelectProduct(id);\\n  }, [onSelectProduct]);\\n\\n  return (\\n    <div className=\\"product-list\\">\\n      {products.map(product => (\\n        <ProductItem\\n          key={product.id}\\n          product={product}\\n          onSelect={handleSelect}\\n        />\\n      ))}\\n    </div>\\n  );\\n};\\n\\nexport default ProductList;"
            }
          ],
          "labels": ["React", "Performance", "Optimization"],
          "reviewComments": [
            "React.memo와 useCallback을 사용한 최적화 전략에 대해 설명해주세요.",
            "이 최적화가 성능에 미치는 구체적인 영향은 무엇인가요?",
            "props comparison function을 추가로 고려해보셨나요?"
          ]
        }
        """;
    }

    private void verifyBackendPr(List<Pr> savedPrs) {
        Pr backendPr = savedPrs.stream()
            .filter(pr -> pr.getTitle().contains("싱글톤"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("백엔드 PR을 찾을 수 없습니다"));
        assertThat(backendPr.getDescription()).contains("Thread-safe");

        List<PrChangedFile> backendFiles = prChangedFileRepository.findByPrId(backendPr.getId());
        assertThat(backendFiles).hasSize(1);
        assertThat(backendFiles.get(0).getFileName()).isEqualTo("src/main/java/com/example/SingletonService.java");
        assertThat(backendFiles.get(0).getLanguage()).isEqualTo("Java");

        List<PrLabel> backendLabels = prLabelRepository.findAllByPrId(backendPr.getId());
        assertThat(backendLabels).hasSize(3);
    }

    private void verifyFrontendPr(List<Pr> savedPrs) {
        Pr frontendPr = savedPrs.stream()
            .filter(pr -> pr.getTitle().contains("리액트"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("프론트엔드 PR을 찾을 수 없습니다"));

        assertThat(frontendPr.getDescription()).contains("React.memo");

        List<PrChangedFile> frontendFiles = prChangedFileRepository.findByPrId(frontendPr.getId());
        assertThat(frontendFiles).hasSize(1);
        assertThat(frontendFiles.get(0).getFileName()).isEqualTo("src/components/ProductList.jsx");
        assertThat(frontendFiles.get(0).getLanguage()).isEqualTo("JavaScript");

        List<PrLabel> frontendLabels = prLabelRepository.findAllByPrId(frontendPr.getId());
        assertThat(frontendLabels).hasSize(3);
    }

    private void initializePrompts() {
        if (promptRepository.findByDeveloperTypeIdAndStudyTypeId(1L, 3L).isEmpty()) {
            promptRepository.save(createBackendPrPrompt());
            promptRepository.save(createFrontendPrPrompt());
        }
    }

    private StudyPrompt createBackendPrPrompt() {
        String generateContent = """
        You are an expert in writing Pull Request examples for backend developers.
        Please respond only with a JSON object in the following format.

        Requirements:
        - "title" and "description" and "reviewComments" must be written in Korean.
        - "fileName", "language", "content", and "labels" should be in English if appropriate.

        JSON Schema:
        {
          "title": "[Concise and clear PR title in Korean]",
          "description": "[Detailed PR description in Korean]",
          "changedFiles": [
            {
              "fileName": "file path and name",
              "language": "programming language",
              "content": "file content"
            }
          ],
          "labels": ["tag1", "tag2", "tag3"],
          "reviewComments": ["question of code review comment 1", "question of code review comment 2", "question of code review comment 3"]
        }
        """;
        return new StudyPrompt(1L, 3L, generateContent);
    }

    private StudyPrompt createFrontendPrPrompt() {
        String generateContent = """
        You are an expert in writing Pull Request examples for frontend developers.
        Please respond only with a JSON object in the following format.

        Requirements:
        - "title" and "description" and "reviewComments" must be written in Korean.
        - "fileName", "language", "content", and "labels" should be in English if appropriate.

        JSON Schema:
        {
          "title": "[Concise and clear PR title in Korean]",
          "description": "[Detailed PR description in Korean]",
          "changedFiles": [
            {
              "fileName": "file path and name",
              "language": "programming language",
              "content": "file content"
            }
          ],
          "labels": ["tag1", "tag2", "tag3"],
          "reviewComments": ["question of code review comment 1", "question of code review comment 2", "question of code review comment 3"]
        }
        """;
        return new StudyPrompt(2L, 3L, generateContent);
    }
}
