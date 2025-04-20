package se.sowl.devlyapi.pr.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.pr.dto.files.PrChangedFilesResponse;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Profile("test")
class PrChangedFilesServiceTest extends MediumTest {

    @AfterEach
    void tearDown() {
        prLabelRepository.deleteAllInBatch();
        prCommentRepository.deleteAllInBatch();
        prChangedFileRepository.deleteAllInBatch();
        prRepository.deleteAllInBatch();
        userStudyRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        developerTypeRepository.deleteAllInBatch();
        studyTypeRepository.deleteAllInBatch();
    }

    @Nested
    class GetChangedFiles {

        @Test
        @DisplayName("특정 PR의 변경 파일을 조회할 수 있다.")
        void get() {
            // given
            DeveloperType developerType = developerTypeRepository.saveAll(getDeveloperTypes()).get(0);
            StudyType studyType = studyTypeRepository.saveAll(getStudyTypes()).get(0);
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            Pr pr = prRepository.save(buildPr(study));
            prChangedFileRepository.saveAll(buildPrChangedFiles(pr));

            // when
            PrChangedFilesResponse changedFilesResponse = prChangedFilesService.getChangedFilesResponse(pr.getId());

            // then
            assertThat(changedFilesResponse.getFiles()).hasSize(2);
            assertThat(changedFilesResponse.getFiles().get(0).getFileName()).isEqualTo("src/main/java/com/example/SingletonService.java");
            assertThat(changedFilesResponse.getFiles().get(1).getFileName()).isEqualTo("src/test/java/com/example/SingletonServiceTest.java");
        }
    }

}
