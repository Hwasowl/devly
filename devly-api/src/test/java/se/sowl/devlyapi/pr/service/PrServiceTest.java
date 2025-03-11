package se.sowl.devlyapi.pr.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.pr.dto.files.PrChangedFilesResponse;
import se.sowl.devlyapi.pr.dto.comments.PrCommentsResponse;
import se.sowl.devlyapi.pr.dto.PrResponse;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PrServiceTest extends MediumTest {

    @AfterEach
    void tearDown() {
        prRepository.deleteAllInBatch();
        prLabelRepository.deleteAllInBatch();
        userStudyRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Nested
    class GetPr {

        @Test
        @DisplayName("특정 스터디의 PR을 조회할 수 있다.")
        void get() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "박정수", "솔", "hwasowl598@gmail.com", "google"));
            Study study = studyRepository.save(buildStudy(2L, 1L));
            userStudyRepository.save(UserStudy.builder().userId(user.getId()).study(study).scheduledAt(LocalDateTime.now()).build());
            Pr pr = prRepository.save(buildPr(study.getId()));
            prLabelRepository.saveAll(buildPrLabels(pr.getId()));

            // when
            PrResponse prResponse = prService.getPr(user.getId(), study.getId());

            // then
            assertThat(prResponse.getTitle()).isEqualTo("싱글톤 패턴 구현");
            assertThat(prResponse.getDescription()).isEqualTo("Thread-safe한 싱글톤 패턴으로 개선");
            assertThat(prResponse.getLabels()).hasSize(3);
            assertThat(prResponse.getLabels()).contains("backend", "feature", "thread");
        }
    }

    @Nested
    class GetChangedFiles {

        @Test
        @DisplayName("특정 PR의 변경 파일을 조회할 수 있다.")
        void get() {
            // given
            Pr pr = prRepository.save(buildPr(1L));
            prChangedFileRepository.saveAll(buildPrChangedFiles(pr.getId()));

            // when
            PrChangedFilesResponse changedFilesResponse = prService.getChangedFiles(pr.getId());

            // then
            assertThat(changedFilesResponse.getFiles()).hasSize(2);
            assertThat(changedFilesResponse.getFiles().get(0).getFileName()).isEqualTo("src/main/java/com/example/SingletonService.java");
            assertThat(changedFilesResponse.getFiles().get(1).getFileName()).isEqualTo("src/test/java/com/example/SingletonServiceTest.java");
        }
    }

    @Nested
    class GetComments {

        @Test
        @DisplayName("특정 PR의 코멘트를 조회할 수 있다.")
        void get() {
            // given
            Pr pr = prRepository.save(buildPr(1L));
            prCommentRepository.saveAll(buildPrComments(pr.getId()));

            // when
            PrCommentsResponse commentsResponse = prService.getComments(pr.getId());

            // then
            assertThat(commentsResponse.getComments()).hasSize(2);
            assertThat(commentsResponse.getComments().get(0).getContent()).isEqualTo("커밋 로그와 변경된 파일을 확인해 어떤 부분을 반영하고 개선한 PR인지 설명해주세요!");
            assertThat(commentsResponse.getComments().get(1).getContent()).isEqualTo("왜 구조가 변경되었는지 상세하게 설명해주세요.");
        }
    }
}
