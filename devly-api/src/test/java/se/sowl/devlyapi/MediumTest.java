package se.sowl.devlyapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.common.jwt.JwtTokenProvider;
import se.sowl.devlyapi.oauth.service.OAuthService;
import se.sowl.devlyapi.study.service.StudyService;
import se.sowl.devlyapi.word.service.WordService;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.repository.UserRepository;
import se.sowl.devlydomain.user.repository.UserStudyRepository;
import se.sowl.devlydomain.word.domain.Word;
import se.sowl.devlydomain.word.repository.WordRepository;

import java.util.List;

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
    protected WordRepository wordRepository;

    @Autowired
    protected StudyRepository studyRepository;

    @Autowired
    protected StudyService studyService;

    @Autowired
    protected StudyTypeRepository studyTypeRepository;

    @Autowired
    protected UserStudyRepository userStudyRepository;

    @Autowired
    protected DeveloperTypeRepository developerTypeRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @MockBean
    protected DefaultOAuth2UserService defaultOAuth2UserService;

    protected User createUser(Long id, Long developerTypeId, String name, String nickname, String email, String provider) {
        return User.builder()
            .id(id)
            .developerTypeId(developerTypeId)
            .name(name)
            .nickname(nickname)
            .email(email)
            .provider(provider)
            .build();
    }

    protected List<Study> generateStudiesOfStudyTypes(List<StudyType> studyTypes, Long developerTypeId) {
        return studyTypes.stream()
            .map(studyType -> buildStudy(studyType.getId(), developerTypeId))
            .toList();
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

    protected static Study buildStudy(Long typeId, Long developerTypeId) {
        return Study.builder()
            .typeId(typeId)
            .developerTypeId(developerTypeId)
            .build();
    }

    // TODO: study detail factory
    protected static List<Word> getBackendWordList(Long studyId) {
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
        Word word4 = Word.builder()
            .word("middleware")
            .pronunciation("/ˈmɪdəlweə/")
            .studyId(studyId)
            .meaning("미들웨어2")
            .example("{\"source\":\"Express Documentation\",\"text\":\"Middleware functions are functions that have access to the request object, the response object, and the next function in the application's request-response cycle.\",\"translation\":\"미들웨어 함수는 요청 객체, 응답 객체, 그리고 애플리케이션의 요청-응답 주기에서 다음 함수에 접근할 수 있는 함수입니다.\"}")
            .quiz("{\"text\":\"\",\"distractors\":[\"Framework\",\"Library\",\"Runtime\",\"Protocol\"]}")
            .build();
        Word word5 = Word.builder()
            .word("middleware")
            .pronunciation("/ˈmɪdəlweə/")
            .studyId(studyId)
            .meaning("미들웨어3")
            .example("{\"source\":\"Express Documentation\",\"text\":\"Middleware functions are functions that have access to the request object, the response object, and the next function in the application's request-response cycle.\",\"translation\":\"미들웨어 함수는 요청 객체, 응답 객체, 그리고 애플리케이션의 요청-응답 주기에서 다음 함수에 접근할 수 있는 함수입니다.\"}")
            .quiz("{\"text\":\"\",\"distractors\":[\"Framework\",\"Library\",\"Runtime\",\"Protocol\"]}")
            .build();
        return List.of(word, word2, word3, word4, word5);
    }
}
