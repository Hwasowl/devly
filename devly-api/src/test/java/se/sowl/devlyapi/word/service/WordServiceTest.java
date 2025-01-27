package se.sowl.devlyapi.word.service;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.dto.WordResponse;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.word.domain.Word;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WordServiceTest extends MediumTest {

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        wordRepository.deleteAllInBatch();
        userStudyRepository.deleteAllInBatch();
    }

    @Nested
    class GetList {
        @Test
        @DisplayName("학습 ID로 소속 단어 목록을 조회할 수 있다.")
        void get() {
            // given
            User user = userRepository.save(createUser(1L, 1L, "박정수", "솔", "hwasowl598@gmail.com", "google"));
            Study study = studyRepository.save(getStudy(2L, 2L));
            wordRepository.saveAll(getWordList(study.getId()));

            userStudyRepository.save(UserStudy.builder().userId(user.getId()).studyId(study.getId()).scheduledAt(LocalDateTime.now()).build());

            // when
            WordListOfStudyResponse list = wordService.getList(user.getId(), study.getId());
            List<WordResponse> words = list.getWords();

            // then
            assertThat(words).hasSize(3);
            AssertionsForClassTypes.assertThat(words.get(0).getWord()).isEqualTo("implementation");
            AssertionsForClassTypes.assertThat(words.get(0).getMeaning()).isEqualTo("구현, 실행");
            AssertionsForClassTypes.assertThat(words.get(1).getWord()).isEqualTo("polymorphism");
            AssertionsForClassTypes.assertThat(words.get(1).getMeaning()).isEqualTo("다형성");
            AssertionsForClassTypes.assertThat(words.get(2).getWord()).isEqualTo("middleware");
            AssertionsForClassTypes.assertThat(words.get(2).getMeaning()).isEqualTo("미들웨어");
        }

        @Test
        @DisplayName("배정되지 않은 학습 ID로 소속 단어 목록을 조회하면 NotAssignmentWordStudyException이 발생한다.")
        void notAssignment () {
            // given
            userRepository.save(createUser(1L, 1L, "박정수", "솔", "hwasowl598@gmail.com", "google"));
            Study study = studyRepository.save(getStudy(2L, 2L));
            wordRepository.saveAll(getWordList(study.getId()));
            // 단어와 스터디는 있지만 유저에게 할당되지 않은 상태

            // when & then
            NotAssignmentWordStudyException exception = assertThrows(
                NotAssignmentWordStudyException.class,
                () -> wordService.getList(1L, 1L));
            assertEquals("아직 학습할 수 없습니다.", exception.getMessage());
        }
    }

    private static List<Word> getWordList(Long studyId) {
        Word word = Word.builder()
            .word("implementation")
            .pronunciation("/ˌɪmplɪmenˈteɪʃən/")
            .studyId(studyId)
            .meaning("구현, 실행")
            .example("{\"source\":\"React Documentation\",\"text\":\"The implementation details of React components should be hidden from their consumers.\",\"translation\":\"React 컴포넌트의 구현 세부사항은 해당 컴포넌트를 사용하는 쪽으로부터 숨겨져야 합니다.\"}")
            .quiz("{\"text\":\"\",\"distractors\":[\"Imitation\",\"Implication\",\"Realization\",\"Deployment\"]}")
            .build();
        Word word2 = Word.builder()
            .word("polymorphism")
            .pronunciation("/ˌpɒlɪˈmɔːfɪzəm/")
            .studyId(studyId)
            .meaning("다형성")
            .example("{\"source\":\"Java Documentation\",\"text\":\"Polymorphism allows you to define one interface and have multiple implementations.\",\"translation\":\"다형성을 통해 하나의 인터페이스를 정의하고 여러 구현을 가질 수 있습니다.\"}")
            .quiz("{\"text\":\"\",\"distractors\":[\"Inheritance\",\"Encapsulation\",\"Abstraction\",\"Interface\"]}")
            .build();
        Word word3 = Word.builder()
            .word("middleware")
            .pronunciation("/ˈmɪdəlweə/")
            .studyId(studyId)
            .meaning("미들웨어")
            .example("{\"source\":\"Express Documentation\",\"text\":\"Middleware functions are functions that have access to the request object, the response object, and the next function in the application's request-response cycle.\",\"translation\":\"미들웨어 함수는 요청 객체, 응답 객체, 그리고 애플리케이션의 요청-응답 주기에서 다음 함수에 접근할 수 있는 함수입니다.\"}")
            .quiz("{\"text\":\"\",\"distractors\":[\"Framework\",\"Library\",\"Runtime\",\"Protocol\"]}")
            .build();
        return List.of(word, word2, word3);
    }

    private static Study getStudy(Long typeId, Long developerTypeId) {
        return Study.builder()
            .typeId(typeId)
            .developerTypeId(developerTypeId)
            .build();
    }
}
