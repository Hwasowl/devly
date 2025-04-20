package se.sowl.devlyapi.pr.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.pr.dto.PrResponse;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PrServiceTest extends MediumTest {

    @Nested
    class GetPr {

        @Test
        @DisplayName("특정 스터디의 PR을 조회할 수 있다.")
        void shouldGetPrForStudy() {
            // given
            List<DeveloperType> developerTypes = createAllDeveloperTypes();
            DeveloperType developerType = developerTypes.stream()
                .filter(type -> type.getName().equals("Backend Developer"))
                .findFirst()
                .orElseThrow();

            User user = userRepository.save(User.builder()
                .id(1L)
                .developerType(developerType)
                .name("박정수")
                .nickname("솔")
                .email("hwasowl598@gmail.com")
                .provider("google")
                .build());

            List<StudyType> studyTypes = studyTypeRepository.saveAll(getStudyTypes());
            StudyType wordStudyType = studyTypes.stream()
                .filter(type -> type.getName().equals("word"))
                .findFirst()
                .orElseThrow();

            Study study = studyRepository.save(Study.builder()
                .studyType(wordStudyType)
                .developerType(developerType)
                .build());

            userStudyRepository.save(UserStudy.builder()
                .user(user)
                .study(study)
                .scheduledAt(LocalDateTime.now())
                .build());

            Pr pr = prRepository.save(Pr.builder()
                .study(study)
                .title("싱글톤 패턴 구현")
                .description("Thread-safe한 싱글톤 패턴으로 개선")
                .build());

            prLabelRepository.saveAll(List.of(
                PrLabel.builder().pr(pr).label("backend").build(),
                PrLabel.builder().pr(pr).label("feature").build(),
                PrLabel.builder().pr(pr).label("thread").build()
            ));

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
        void shouldCompletePr() {
            // given
            List<DeveloperType> developerTypes = createAllDeveloperTypes();
            DeveloperType developerType = developerTypes.stream()
                .filter(type -> type.getName().equals("Backend Developer"))
                .findFirst()
                .orElseThrow();

            User user = userRepository.save(User.builder()
                .id(1L)
                .developerType(developerType)
                .name("박정수")
                .nickname("솔")
                .email("hwasowl598@gmail.com")
                .provider("google")
                .build());

            List<StudyType> studyTypes = createAllStudyTypes();
            StudyType prStudyType = studyTypes.stream()
                .filter(type -> type.getName().equals("pr"))
                .findFirst()
                .orElseThrow();

            Study study = studyRepository.save(Study.builder()
                .studyType(prStudyType)
                .developerType(developerType)
                .build());

            userStudyRepository.save(UserStudy.builder()
                .user(user)
                .study(study)
                .scheduledAt(LocalDateTime.now())
                .build());

            Pr pr = prRepository.save(Pr.builder()
                .study(study)
                .title("싱글톤 패턴 구현")
                .description("Thread-safe한 싱글톤 패턴으로 개선")
                .build());

            // when
            prService.complete(user.getId(), pr.getId(), study.getId());

            // then
            UserStudy userStudy = userStudyRepository.findByUserIdAndStudyId(user.getId(), study.getId()).orElseThrow();
            assertThat(userStudy.isCompleted()).isTrue();
        }
    }
}
