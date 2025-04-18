package se.sowl.devlybatch.job.pr;

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
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.pr.repository.PrChangedFileRepository;
import se.sowl.devlydomain.pr.repository.PrLabelRepository;
import se.sowl.devlydomain.pr.repository.PrRepository;
import se.sowl.devlydomain.prompt.domain.GeneratePrompt;
import se.sowl.devlydomain.prompt.repository.GeneratePromptRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatusEnum;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlyexternal.client.gpt.GPTClient;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestBatchConfig.class, PrCreationJobConfig.class})
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
    private GeneratePromptRepository promptRepository;

    @MockBean
    private GPTClient gptClient;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
        jobLauncherTestUtils.setJob(prCreationJob);
        jobLauncherTestUtils.setJobRepository(jobRepository);

        if (promptRepository.findFirstByDeveloperTypeIdAndStudyTypeId(1L, 3L).isEmpty()) {
            GeneratePrompt backendGeneratePrompt = new GeneratePrompt(1L, 3L, "백엔드 개발자를 위한 PR(Pull Request) 예시를 생성해주세요.\n" +
                "각 PR은 다음 형식으로 정확히 작성해주세요:\n" +
                "제목: [PR의 간결하고 명확한 제목]\n" +
                "설명: [PR에 대한 자세한 설명]\n" +
                "변경 파일: [{\"fileName\": \"파일 경로와 이름\", \"language\": \"프로그래밍 언어\", \"content\": \"파일 내용\"}]\n" +
                "라벨: [\"변경 파일에 해당하는 태그1\", \"변경 파일에 해당하는 태그2\", \"변경 파일에 해당하는 태그3\"]\n" +
                "---");
            promptRepository.save(backendGeneratePrompt);

            GeneratePrompt frontendGeneratePrompt = new GeneratePrompt(2L, 3L, "프론트엔드 개발자를 위한 PR(Pull Request) 예시를 생성해주세요.\n" +
                "각 PR은 다음 형식으로 정확히 작성해주세요:\n" +
                "제목: [PR의 간결하고 명확한 제목]\n" +
                "설명: [PR에 대한 자세한 설명]\n" +
                "변경 파일: [{\"fileName\": \"파일 경로와 이름\", \"language\": \"프로그래밍 언어\", \"content\": \"파일 내용\"}]\n" +
                "라벨: [\"변경 파일에 해당하는 태그1\", \"변경 파일에 해당하는 태그2\", \"변경 파일에 해당하는 태그3\"]\n" +
                "---");
            promptRepository.save(frontendGeneratePrompt);
        }
        studyTypeRepository.saveAll(List.of(
            StudyType.builder().name("Word").build(),
            StudyType.builder().name("Knowledge").build(),
            StudyType.builder().name("PR").build(),
            StudyType.builder().name("Discussion").build()
        ));
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
    void createPrStepTest() throws Exception {
        // given
        StudyType studyType = studyTypeRepository.findAll().stream().filter(s -> s.getName().equals("PR")).findFirst().orElseThrow();
        DeveloperType beType = developerTypeRepository.save(DeveloperType.builder().name("Backend").build());
        DeveloperType feType = developerTypeRepository.save(DeveloperType.builder().name("Frontend").build());
        Study backendStudy = buildStudy(studyType, beType);
        Study frontendStudy = buildStudy(studyType, feType);
        studyRepository.saveAll(List.of(backendStudy, frontendStudy));

        String backendResponse = """
        제목: 싱글톤 패턴 구현 개선
        설명: Thread-safe한 싱글톤 패턴으로 개선하고, 불필요한 메모리 사용을 줄였습니다.
        변경 파일: [{"fileName": "src/main/java/com/example/SingletonService.java", "language": "Java", "content": "public class SingletonService {\\n\\n    private static volatile SingletonService instance;\\n\\n    private SingletonService() {\\n        // private constructor\\n    }\\n\\n    public static SingletonService getInstance() {\\n        if (instance == null) {\\n            synchronized (SingletonService.class) {\\n                if (instance == null) {\\n                    instance = new SingletonService();\\n                }\\n            }\\n        }\\n        return instance;\\n    }\\n}"}]
        라벨: ["Java", "Thread-safe", "Singleton"]
        ---
        """;

        String frontendResponse = """
        제목: 리액트 컴포넌트 최적화
        설명: 불필요한 렌더링을 방지하기 위해 React.memo와 useCallback 훅을 적용했습니다.
        변경 파일: [{"fileName": "src/components/ProductList.jsx", "language": "JavaScript", "content": "import React, { useCallback } from 'react';\\n\\nconst ProductItem = React.memo(({ product, onSelect }) => {\\n  return (\\n    <div className=\\"product-item\\" onClick={() => onSelect(product.id)}>\\n      <h3>{product.name}</h3>\\n      <p>{product.price}</p>\\n    </div>\\n  );\\n});\\n\\nconst ProductList = ({ products, onSelectProduct }) => {\\n  const handleSelect = useCallback((id) => {\\n    onSelectProduct(id);\\n  }, [onSelectProduct]);\\n\\n  return (\\n    <div className=\\"product-list\\">\\n      {products.map(product => (\\n        <ProductItem\\n          key={product.id}\\n          product={product}\\n          onSelect={handleSelect}\\n        />\\n      ))}\\n    </div>\\n  );\\n};\\n\\nexport default ProductList;"}]
        라벨: ["React", "Performance", "Optimization"]
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
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("createPrStep");

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        verify(gptClient, times(2)).generate(any());

        studyRepository.findAll().forEach(study -> {
            assertThat(study.getStatus()).isEqualTo(StudyStatusEnum.CONNECTED);
        });

        List<Pr> savedPrs = prRepository.findAll();
        assertThat(savedPrs).hasSize(2);

        Pr backendPr = savedPrs.stream()
            .filter(pr -> pr.getTitle().contains("싱글톤"))
            .findFirst()
            .orElseThrow();
        assertThat(backendPr.getDescription()).contains("Thread-safe");

        List<PrChangedFile> backendFiles = prChangedFileRepository.findByPrId(backendPr.getId());
        assertThat(backendFiles).hasSize(1);
        assertThat(backendFiles.get(0).getFileName()).isEqualTo("src/main/java/com/example/SingletonService.java");
        assertThat(backendFiles.get(0).getLanguage()).isEqualTo("Java");

        List<PrLabel> backendLabels = prLabelRepository.findAllByPrId(backendPr.getId());
        assertThat(backendLabels).hasSize(3);

        Pr frontendPr = savedPrs.stream()
            .filter(pr -> pr.getTitle().contains("리액트"))
            .findFirst()
            .orElseThrow();
        assertThat(frontendPr.getDescription()).contains("React.memo");

        List<PrChangedFile> frontendFiles = prChangedFileRepository.findByPrId(frontendPr.getId());
        assertThat(frontendFiles).hasSize(1);
        assertThat(frontendFiles.get(0).getFileName()).isEqualTo("src/components/ProductList.jsx");
        assertThat(frontendFiles.get(0).getLanguage()).isEqualTo("JavaScript");

        List<PrLabel> frontendLabels = prLabelRepository.findAllByPrId(frontendPr.getId());
        assertThat(frontendLabels).hasSize(3);
    }
}
