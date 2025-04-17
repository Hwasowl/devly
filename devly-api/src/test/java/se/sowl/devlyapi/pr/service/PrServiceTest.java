package se.sowl.devlyapi.pr.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.pr.dto.PrResponse;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
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
            DeveloperType developerType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
            User user = userRepository.save(createUser(1L, developerType, "박정수", "솔", "hwasowl598@gmail.com", "google"));
            StudyType studyType = studyTypeRepository.save(new StudyType("Word", 100L));
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            userStudyRepository.save(UserStudy.builder().user(user).study(study).scheduledAt(LocalDateTime.now()).build());
            Pr pr = prRepository.save(buildPr(study.getId()));
            prLabelRepository.saveAll(buildPrLabels(pr.getId()));

            // when
            PrResponse prResponse = prService.getPrResponse(user.getId(), study.getId());

            // then
            assertThat(prResponse.getTitle()).isEqualTo("싱글톤 패턴 구현");
            assertThat(prResponse.getDescription()).isEqualTo("Thread-safe한 싱글톤 패턴으로 개선");
            assertThat(prResponse.getLabels()).hasSize(3);
            assertThat(prResponse.getLabels()).contains("backend", "feature", "thread");
        }
    }

    @Nested
    class CompletePr {

        @Test
        @DisplayName("특정 스터디의 PR을 완료할 수 있다.")
        void complete() {
            // given
            DeveloperType developerType = developerTypeRepository.save(new DeveloperType("Backend Developer"));
            User user = userRepository.save(createUser(1L, developerType, "박정수", "솔", "hwasowl598@gmail.com", "google"));
            StudyType studyType = studyTypeRepository.save(new StudyType("PR", 100L));
            Study study = studyRepository.save(buildStudy(studyType, developerType));
            userStudyRepository.save(UserStudy.builder().user(user).study(study).scheduledAt(LocalDateTime.now()).build());
            Pr pr = prRepository.save(buildPr(study.getId()));

            // when
            prService.complete(user.getId(), pr.getId(), study.getId());

            // then
            UserStudy userStudy = userStudyRepository.findByUserIdAndStudyId(user.getId(), study.getId()).orElseThrow();
            assertThat(userStudy.isCompleted()).isTrue();
        }
    }
}
