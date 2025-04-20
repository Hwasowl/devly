package se.sowl.devlyapi.pr.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.pr.dto.files.PrChangedFilesResponse;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Profile("test")
class PrChangedFilesServiceTest extends MediumTest {

    @Test
    @DisplayName("특정 PR의 변경 파일을 조회할 수 있다.")
    void shouldGetChangedFilesForPr() {
        // given
        List<DeveloperType> developerTypes = createAllDeveloperTypes();
        DeveloperType developerType = developerTypes.get(0);

        List<StudyType> studyTypes = createAllStudyTypes();
        StudyType studyType = studyTypes.get(0);

        Study study = studyRepository.save(Study.builder()
            .studyType(studyType)
            .developerType(developerType)
            .build());

        Pr pr = prRepository.save(Pr.builder()
            .study(study)
            .title("싱글톤 패턴 구현")
            .description("Thread-safe한 싱글톤 패턴으로 개선")
            .build());

        prChangedFileRepository.saveAll(List.of(
            PrChangedFile.builder().pr(pr).fileName("src/main/java/com/example/SingletonService.java").build(),
            PrChangedFile.builder().pr(pr).fileName("src/test/java/com/example/SingletonServiceTest.java").build()
        ));

        // when
        PrChangedFilesResponse changedFilesResponse = prChangedFilesService.getChangedFilesResponse(pr.getId());

        // then
        assertThat(changedFilesResponse.getFiles()).hasSize(2);
        assertThat(changedFilesResponse.getFiles().get(0).getFileName()).isEqualTo("src/main/java/com/example/SingletonService.java");
        assertThat(changedFilesResponse.getFiles().get(1).getFileName()).isEqualTo("src/test/java/com/example/SingletonServiceTest.java");
    }
}
