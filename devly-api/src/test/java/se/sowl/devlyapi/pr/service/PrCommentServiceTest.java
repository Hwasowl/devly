package se.sowl.devlyapi.pr.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.pr.dto.comments.PrCommentsResponse;
import se.sowl.devlydomain.pr.domain.Pr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Profile("test")
class PrCommentServiceTest extends MediumTest {

    @AfterEach
    void tearDown() {
        prRepository.deleteAllInBatch();
        prLabelRepository.deleteAllInBatch();
        userStudyRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
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
            PrCommentsResponse commentsResponse = prCommentService.getCommentsResponse(pr.getId());

            // then
            assertThat(commentsResponse.getComments()).hasSize(2);
            assertThat(commentsResponse.getComments().get(0).getContent()).isEqualTo("커밋 로그와 변경된 파일을 확인해 어떤 부분을 반영하고 개선한 PR인지 설명해주세요!");
            assertThat(commentsResponse.getComments().get(1).getContent()).isEqualTo("왜 구조가 변경되었는지 상세하게 설명해주세요.");
        }
    }

}
