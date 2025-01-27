package se.sowl.devlyapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.oauth.service.OAuthService;
import se.sowl.devlyapi.word.service.WordService;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.repository.UserRepository;
import se.sowl.devlydomain.user.repository.UserStudyRepository;
import se.sowl.devlydomain.word.repository.WordRepository;

@SpringBootTest
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
    protected UserStudyRepository userStudyRepository;

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
}
