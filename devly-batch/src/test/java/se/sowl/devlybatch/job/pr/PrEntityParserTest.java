package se.sowl.devlybatch.job.pr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sowl.devlybatch.common.JsonExtractor;
import se.sowl.devlybatch.job.MediumBatchTest;
import se.sowl.devlybatch.job.pr.dto.PrWithRelations;
import se.sowl.devlybatch.job.pr.service.PrEntityParser;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;
import se.sowl.devlyexternal.common.ParserArguments;
import se.sowl.devlyexternal.common.gpt.GptRequestFactory;
import se.sowl.devlyexternal.common.gpt.GptResponseValidator;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PrEntityParserTest extends MediumBatchTest {

    private final PrEntityParser prEntityParser;

    @Autowired
    public PrEntityParserTest(StudyRepository studyRepository) {
        this.prEntityParser = new PrEntityParser(
            new JsonExtractor(new ObjectMapper()),
            new GptRequestFactory(),
            new GptResponseValidator(),
            studyRepository
        );
    }

    @Test
    @DisplayName("PR GPT 응답을 파싱하여 정확한 PR 엔티티 및 관계 객체로 변환한다")
    void shouldParseGptResponseToPrEntitiesWithRelations() {
        // given
        DeveloperType developerType = developerTypeRepository.saveAll(createStandardDeveloperTypes()).get(0);
        StudyType studyType = studyTypeRepository.saveAll(createStandardStudyTypes()).get(0);
        Study study = studyRepository.save(buildStudy(studyType, developerType));
        Long studyId = study.getId();

        String responseContent = """
           제목: 싱글톤 패턴 구현 개선
           설명: Thread-safe한 싱글톤 패턴으로 개선하고, 불필요한 메모리 사용을 줄였습니다.
           변경 파일: [{"fileName": "src/main/java/com/example/SingletonService.java", "language": "Java", "content": "public class SingletonService {\\n\\n    private static volatile SingletonService instance;\\n\\n    private SingletonService() {\\n        // private constructor\\n    }\\n\\n    public static SingletonService getInstance() {\\n        if (instance == null) {\\n            synchronized (SingletonService.class) {\\n                if (instance == null) {\\n                    instance = new SingletonService();\\n                }\\n            }\\n        }\\n        return instance;\\n    }\\n}"},{"fileName": "src/test/java/com/example/SingletonServiceTest.java", "language": "Java", "content": "import org.junit.jupiter.api.Test;\\nimport static org.junit.jupiter.api.Assertions.*;\\n\\npublic class SingletonServiceTest {\\n\\n    @Test\\n    void testSingletonInstance() {\\n        SingletonService instance1 = SingletonService.getInstance();\\n        SingletonService instance2 = SingletonService.getInstance();\\n        assertSame(instance1, instance2);\\n    }\\n}"}]
           라벨: ["Java", "Thread-safe", "Singleton", "Design Pattern", "Performance"]
           질문: ["싱글톤 패턴을 사용하는 이유는 무엇인가요?", "volatile 키워드의 역할은 무엇인가요?"]
           ---
           """;
        GPTResponse gptResponse = createGptResponse(responseContent);

        // when
        ParserArguments parseParameters = new ParserArguments().add("studyId", studyId);
        List<PrWithRelations> prWithRelations = prEntityParser.parseGPTResponse(gptResponse, parseParameters);

        // then
        assertThat(prWithRelations).hasSize(1);

        PrWithRelations prWithRelation = prWithRelations.get(0);
        Pr pr = prWithRelation.getPr();
        assertPrBasicProperties(pr, studyId);

        List<PrChangedFile> changedFiles = prWithRelation.getChangedFiles();
        assertChangedFiles(changedFiles);

        List<PrLabel> labels = prWithRelation.getLabels();
        assertLabels(labels);

        List<PrComment> comments = prWithRelation.getComments();
        assertComments(comments);
    }

    private void assertPrBasicProperties(Pr pr, Long studyId) {
        assertThat(pr.getTitle()).isEqualTo("싱글톤 패턴 구현 개선");
        assertThat(pr.getDescription()).isEqualTo("Thread-safe한 싱글톤 패턴으로 개선하고, 불필요한 메모리 사용을 줄였습니다.");
        assertThat(pr.getStudy().getId()).isEqualTo(studyId);
    }

    private void assertChangedFiles(List<PrChangedFile> changedFiles) {
        assertThat(changedFiles).hasSize(2);

        PrChangedFile singletonServiceFile = changedFiles.stream()
            .filter(file -> file.getFileName().contains("SingletonService.java"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("SingletonService.java 파일을 찾을 수 없습니다"));
        assertThat(singletonServiceFile.getLanguage()).isEqualTo("Java");
        assertThat(singletonServiceFile.getContent()).contains("volatile SingletonService instance");

        PrChangedFile testFile = changedFiles.stream()
            .filter(file -> file.getFileName().contains("SingletonServiceTest.java"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("SingletonServiceTest.java 파일을 찾을 수 없습니다"));
        assertThat(testFile.getLanguage()).isEqualTo("Java");
        assertThat(testFile.getContent()).contains("testSingletonInstance");
    }

    private void assertLabels(List<PrLabel> labels) {
        assertThat(labels).hasSize(5);

        List<String> expectedLabels = List.of("Java", "Thread-safe", "Singleton", "Design Pattern", "Performance");
        for (int i = 0; i < labels.size(); i++) {
            String expectedLabel = expectedLabels.get(i);
            String actualLabel = labels.get(i).getLabel();

            assertThat(actualLabel).isEqualTo(expectedLabel);
        }
    }

    private void assertComments(List<PrComment> comments) {
        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getContent()).contains("커밋 로그와 변경된 파일을 확인해 어떤 부분을 반영하고 개선한 PR인지 설명해주세요!");
    }
}
