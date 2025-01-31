package se.sowl.devlyapi.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.study.service.StudyService;
import se.sowl.devlydomain.oauth.domain.OAuth2Profile;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final StudyService studyService;

    public List<User> getList() {
        return userRepository.findAll();
    }

    public User getOrCreateUser(OAuth2Profile oAuth2Profile, Long developerType) {
        return userRepository.findByEmailAndProvider(oAuth2Profile.getEmail(), oAuth2Profile.getProvider())
            .orElseGet(() -> {
                User savedUser = saveUser(oAuth2Profile, developerType);
                studyService.initialUserStudies(savedUser);
                return savedUser;
            });
    }

    private User saveUser(OAuth2Profile oAuth2Profile, Long developerType) {
        User user = User.builder()
            .name(oAuth2Profile.getName())
            .email(oAuth2Profile.getEmail())
            .provider(oAuth2Profile.getProvider())
            .developerTypeId(developerType)
            .build();
        return userRepository.save(user);
    }
}
