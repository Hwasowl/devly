package se.sowl.devlyapi.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.study.service.StudyService;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.oauth.domain.OAuth2Profile;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final DeveloperTypeRepository developerTypeRepository;
    private final StudyService studyService;

    public List<User> getList() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User getOrCreateUser(OAuth2Profile oAuth2Profile, Long developerType) {
        DeveloperType type = developerTypeRepository.findById(developerType).orElseThrow(
            () -> new IllegalArgumentException("Developer type not found")
        );
        return userRepository.findByEmailAndProvider(oAuth2Profile.getEmail(), oAuth2Profile.getProvider())
            .orElseGet(() -> {
                User savedUser = saveUser(oAuth2Profile, type);
                studyService.initialUserStudies(savedUser);
                return savedUser;
            });
    }

    private User saveUser(OAuth2Profile oAuth2Profile, DeveloperType developerType) {
        User user = User.builder()
            .name(oAuth2Profile.getName())
            .email(oAuth2Profile.getEmail())
            .provider(oAuth2Profile.getProvider())
            .developerType(developerType)
            .build();
        return userRepository.save(user);
    }
}
