package se.sowl.devlyapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.common.jwt.JwtTokenProvider;
import se.sowl.devlyapi.oauth.service.OAuthService;
import se.sowl.devlyapi.pr.service.PrChangedFilesService;
import se.sowl.devlyapi.pr.service.PrCommentService;
import se.sowl.devlyapi.pr.service.PrReviewService;
import se.sowl.devlyapi.pr.service.PrService;
import se.sowl.devlyapi.study.service.StudyService;
import se.sowl.devlyapi.study.service.UserStudyService;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlyapi.word.service.WordReviewService;
import se.sowl.devlyapi.word.service.WordService;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.repository.*;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserRepository;
import se.sowl.devlydomain.user.repository.UserStudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.domain.WordReview;
import se.sowl.devlydomain.word.repository.WordRepository;
import se.sowl.devlydomain.word.repository.WordReviewRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class MediumTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected OAuthService oAuthService;

    @Autowired
    protected WordService wordService;

    @Autowired
    protected WordReviewService wordReviewService;

    @Autowired
    protected StudyService studyService;

    @Autowired
    protected PrService prService;

    @Autowired
    protected PrChangedFilesService prChangedFilesService;

    @Autowired
    protected PrCommentService prCommentService;

    @Autowired
    protected PrReviewService prReviewService;

    @Autowired
    protected UserStudyService userStudyService;

    @Autowired
    protected WordRepository wordRepository;

    @Autowired
    protected WordReviewRepository wordReviewRepository;

    @Autowired
    protected StudyRepository studyRepository;

    @Autowired
    protected StudyTypeRepository studyTypeRepository;

    @Autowired
    protected UserStudyRepository userStudyRepository;

    @Autowired
    protected DeveloperTypeRepository developerTypeRepository;

    @Autowired
    protected PrRepository prRepository;

    @Autowired
    protected PrLabelRepository prLabelRepository;

    @Autowired
    protected PrChangedFileRepository prChangedFileRepository;

    @Autowired
    protected PrCommentRepository prCommentRepository;

    @Autowired
    protected PrReviewRepository prReviewRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @MockBean
    protected DefaultOAuth2UserService defaultOAuth2UserService;

    // 표준 tearDown 메서드
    @AfterEach
    protected void baseTearDown() {
        wordReviewRepository.deleteAllInBatch();
        prLabelRepository.deleteAllInBatch();
        prChangedFileRepository.deleteAllInBatch();
        prReviewRepository.deleteAllInBatch();
        prCommentRepository.deleteAllInBatch();
        prRepository.deleteAllInBatch();
        userStudyRepository.deleteAllInBatch();
        wordRepository.deleteAllInBatch();
        studyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        developerTypeRepository.deleteAllInBatch();
        studyTypeRepository.deleteAllInBatch();
    }

    protected User createUser(DeveloperType developerType, String name, String nickname, String email, String provider) {
        return User.builder()
            .name(name)
            .developerType(developerType)
            .nickname(nickname)
            .email(email)
            .provider(provider)
            .build();
    }

    protected List<StudyType> getStudyTypes() {
        StudyType word = new StudyType("word", 500L);
        StudyType knowledge = new StudyType("knowledge", 1000L);
        StudyType pr = new StudyType("pr", 3000L);
        StudyType discussion = new StudyType("discussion", 2000L);
        return List.of(word, knowledge, pr, discussion);
    }

    protected List<DeveloperType> getDeveloperTypes() {
        DeveloperType frontEnd = new DeveloperType("Backend Developer");
        DeveloperType backEnd = new DeveloperType("Frontend Developer");
        return List.of(frontEnd, backEnd);
    }

    protected List<DeveloperType> createAllDeveloperTypes() {
        return developerTypeRepository.saveAll(getDeveloperTypes());
    }

    protected List<StudyType> createAllStudyTypes() {
        return studyTypeRepository.saveAll(getStudyTypes());
    }

    protected Study buildStudy(StudyType studyType, DeveloperType developerType) {
        return Study.builder()
            .studyType(studyType)
            .developerType(developerType)
            .build();
    }

    protected Pr buildPr(Study study) {
        return Pr.builder()
            .title("싱글톤 패턴 구현")
            .description("Thread-safe한 싱글톤 패턴으로 개선")
            .study(study)
            .build();
    }

    protected List<PrComment> buildPrComments(Pr pr) {
        return List.of(
            PrComment.builder()
                .pr(pr)
                .sequence(1L)
                .content("커밋 로그와 변경된 파일을 확인해 어떤 부분을 반영하고 개선한 PR인지 설명해주세요!")
                .build(),
            PrComment.builder()
                .pr(pr)
                .sequence(2L)
                .content("왜 구조가 변경되었는지 상세하게 설명해주세요.")
                .build()
        );
    }

    protected DeveloperType createBackendDeveloperType() {
        return developerTypeRepository.save(new DeveloperType("Backend Developer"));
    }

    protected StudyType createWordStudyType() {
        return studyTypeRepository.save(new StudyType("word", 100L));
    }

    protected User createTestUser(DeveloperType developerType) {
        return createTestUser(null, developerType, "테스트유저", "닉네임", "test@example.com", "google");
    }

    protected User createTestUser(Long id, DeveloperType developerType, String name, String nickname, String email, String provider) {
        return userRepository.save(User.builder()
            .id(id)
            .developerType(developerType)
            .name(name)
            .nickname(nickname)
            .email(email)
            .provider(provider)
            .build());
    }

    protected Study createStudy(StudyType studyType, DeveloperType developerType) {
        return studyRepository.save(Study.builder()
            .studyType(studyType)
            .developerType(developerType)
            .build());
    }

    protected UserStudy assignUserToStudy(User user, Study study) {
        return userStudyRepository.save(
            UserStudy.builder()
                .user(user)
                .study(study)
                .scheduledAt(LocalDateTime.now())
                .build()
        );
    }

    protected List<Word> createBackendWords(Study study) {
        return wordRepository.saveAll(getBackendWordList(study));
    }

    protected void assertWordReviews(List<WordReview> reviews, List<Long> correctIds) {
        reviews.forEach(review -> {
            if (correctIds.contains(review.getWord().getId())) {
                assertThat(review.isCorrect()).isTrue();
            } else {
                assertThat(review.isCorrect()).isFalse();
            }
        });
    }

    protected void assertStudyCompleted(User user, Study study) {
        UserStudy userStudy = userStudyRepository.findByUserIdAndStudyId(user.getId(), study.getId())
            .orElseThrow(NotAssignmentWordStudyException::new);
        assertThat(userStudy.isCompleted()).isTrue();
    }

    protected static List<Word> getBackendWordList(Study study) {
        Word word = Word.builder()
            .word("implementation")
            .pronunciation("/ˌɪmplɪmenˈteɪʃən/")
            .study(study)
            .meaning("구현, 실행")
            .example("{\"source\":\"React Documentation\",\"text\":\"The implementation details of React components should be hidden from their consumers.\",\"translation\":\"React 컴포넌트의 구현 세부사항은 해당 컴포넌트를 사용하는 쪽으로부터 숨겨져야 합니다.\"}")
            .quiz("{\"text\":\"\",\"distractors\":[\"Imitation\",\"Implication\",\"Realization\",\"Deployment\"]}")
            .build();
        Word word2 = Word.builder()
            .word("polymorphism")
            .pronunciation("/ˌpɒlɪˈmɔːfɪzəm/")
            .study(study)
            .meaning("다형성")
            .example("{\"source\":\"Java Documentation\",\"text\":\"Polymorphism allows you to define one interface and have multiple implementations.\",\"translation\":\"다형성을 통해 하나의 인터페이스를 정의하고 여러 구현을 가질 수 있습니다.\"}")
            .quiz("{\"text\":\"\",\"distractors\":[\"Inheritance\",\"Encapsulation\",\"Abstraction\",\"Interface\"]}")
            .build();
        Word word3 = Word.builder()
            .word("middleware")
            .pronunciation("/ˈmɪdəlweə/")
            .study(study)
            .meaning("미들웨어")
            .example("{\"source\":\"Express Documentation\",\"text\":\"Middleware functions are functions that have access to the request object, the response object, and the next function in the application's request-response cycle.\",\"translation\":\"미들웨어 함수는 요청 객체, 응답 객체, 그리고 애플리케이션의 요청-응답 주기에서 다음 함수에 접근할 수 있는 함수입니다.\"}")
            .quiz("{\"text\":\"\",\"distractors\":[\"Framework\",\"Library\",\"Runtime\",\"Protocol\"]}")
            .build();
        Word word4 = Word.builder()
            .word("middleware")
            .pronunciation("/ˈmɪdəlweə/")
            .study(study)
            .meaning("미들웨어2")
            .example("{\"source\":\"Express Documentation\",\"text\":\"Middleware functions are functions that have access to the request object, the response object, and the next function in the application's request-response cycle.\",\"translation\":\"미들웨어 함수는 요청 객체, 응답 객체, 그리고 애플리케이션의 요청-응답 주기에서 다음 함수에 접근할 수 있는 함수입니다.\"}")
            .quiz("{\"text\":\"\",\"distractors\":[\"Framework\",\"Library\",\"Runtime\",\"Protocol\"]}")
            .build();
        Word word5 = Word.builder()
            .word("middleware")
            .pronunciation("/ˈmɪdəlweə/")
            .study(study)
            .meaning("미들웨어3")
            .example("{\"source\":\"Express Documentation\",\"text\":\"Middleware functions are functions that have access to the request object, the response object, and the next function in the application's request-response cycle.\",\"translation\":\"미들웨어 함수는 요청 객체, 응답 객체, 그리고 애플리케이션의 요청-응답 주기에서 다음 함수에 접근할 수 있는 함수입니다.\"}")
            .quiz("{\"text\":\"\",\"distractors\":[\"Framework\",\"Library\",\"Runtime\",\"Protocol\"]}")
            .build();
        return List.of(word, word2, word3, word4, word5);
    }
}
