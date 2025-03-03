package se.sowl.devlybatch.job.pr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlybatch.job.pr.utils.PrContentProcessor;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.pr.repository.PrChangedFileRepository;
import se.sowl.devlydomain.pr.repository.PrLabelRepository;
import se.sowl.devlydomain.pr.repository.PrRepository;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PrContentProcessorTest {

    @Autowired
    private PrContentProcessor prContentProcessor;

    @Autowired
    private PrRepository prRepository;

    @Autowired
    private PrChangedFileRepository prChangedFileRepository;

    @Autowired
    private PrLabelRepository prLabelRepository;

    @Test
    @DisplayName("PR GPT 응답을 파싱 처리 해 PR 엔티티로 변환한다")
    void parseGPTResponseTest() {
        // given
        Long studyId = 1L;
        String responseContent = """
           제목: 싱글톤 패턴 구현 개선
           설명: Thread-safe한 싱글톤 패턴으로 개선하고, 불필요한 메모리 사용을 줄였습니다.
           변경 파일: [{"fileName": "src/main/java/com/example/SingletonService.java", "language": "Java", "content": "public class SingletonService {\\n\\n    private static volatile SingletonService instance;\\n\\n    private SingletonService() {\\n        // private constructor\\n    }\\n\\n    public static SingletonService getInstance() {\\n        if (instance == null) {\\n            synchronized (SingletonService.class) {\\n                if (instance == null) {\\n                    instance = new SingletonService();\\n                }\\n            }\\n        }\\n        return instance;\\n    }\\n}"},{"fileName": "src/test/java/com/example/SingletonServiceTest.java", "language": "Java", "content": "import org.junit.jupiter.api.Test;\\nimport static org.junit.jupiter.api.Assertions.*;\\n\\npublic class SingletonServiceTest {\\n\\n    @Test\\n    void testSingletonInstance() {\\n        SingletonService instance1 = SingletonService.getInstance();\\n        SingletonService instance2 = SingletonService.getInstance();\\n        assertSame(instance1, instance2);\\n    }\\n}"}]
           라벨: ["Java", "Thread-safe", "Singleton", "Design Pattern", "Performance"]
           ---
           """;
        GPTResponse gptResponse = new GPTResponse(
            List.of(new GPTResponse.Choice(
                new GPTResponse.Message("assistant", responseContent),
                "stop",
                0
            ))
        );

        // DB 초기화 (이전 테스트 데이터 제거)
        prLabelRepository.deleteAll();
        prChangedFileRepository.deleteAll();
        prRepository.deleteAll();

        // when
        List<Pr> prs = prContentProcessor.parseGPTResponse(gptResponse, studyId);

        // then
        assertThat(prs).hasSize(1);

        // PR 검증
        Pr pr = prs.getFirst();
        assertThat(pr.getTitle()).isEqualTo("싱글톤 패턴 구현 개선");
        assertThat(pr.getDescription()).isEqualTo("Thread-safe한 싱글톤 패턴으로 개선하고, 불필요한 메모리 사용을 줄였습니다.");

        // 저장된 PR 조회
        List<Pr> savedPrs = prRepository.findAll();
        assertThat(savedPrs).hasSize(1);
        Long savedPrId = savedPrs.get(0).getId();

        // Changed Files 저장 검증
        List<PrChangedFile> changedFiles = prChangedFileRepository.findByPullRequestId(savedPrId);
        assertThat(changedFiles).hasSize(2);

        // 파일 내용 검증
        PrChangedFile singletonServiceFile = changedFiles.stream()
            .filter(file -> file.getFileName().contains("SingletonService.java"))
            .findFirst()
            .orElseThrow();

        assertThat(singletonServiceFile.getLanguage()).isEqualTo("Java");
        assertThat(singletonServiceFile.getContent()).contains("volatile SingletonService instance");

        PrChangedFile testFile = changedFiles.stream()
            .filter(file -> file.getFileName().contains("SingletonServiceTest.java"))
            .findFirst()
            .orElseThrow();

        assertThat(testFile.getLanguage()).isEqualTo("Java");
        assertThat(testFile.getContent()).contains("testSingletonInstance");

        // 라벨 저장 검증
        List<PrLabel> labels = prLabelRepository.findAllByPrId(savedPrId);
        assertThat(labels).hasSize(5);
    }
}
