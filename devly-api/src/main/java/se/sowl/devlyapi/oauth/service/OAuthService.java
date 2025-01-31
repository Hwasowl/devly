package se.sowl.devlyapi.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.oauth.exception.OAuth2AuthenticationProcessingException;
import se.sowl.devlyapi.oauth.exception.OAuth2ProviderNotSupportedException;
import se.sowl.devlyapi.oauth.factory.OAuth2UserFactory;
import se.sowl.devlyapi.user.service.UserService;
import se.sowl.devlydomain.oauth.domain.OAuth2Extractor;
import se.sowl.devlydomain.oauth.domain.OAuth2Profile;
import se.sowl.devlydomain.oauth.domain.OAuth2Provider;
import se.sowl.devlydomain.user.domain.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final DefaultOAuth2UserService defaultOAuth2UserService;
    private final OAuth2UserFactory oAuth2UserFactory;
    private final UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        try {
            OAuth2User loadedUser = defaultOAuth2UserService.loadUser(userRequest);
            OAuth2Profile profile = extractOAuth2Profile(userRequest, loadedUser);
            Long developerType = extractDeveloperType(userRequest);

            User user = userService.getOrCreateUser(profile, developerType);
            OAuth2User oAuth2User = oAuth2UserFactory.createOAuth2User(userRequest, loadedUser, profile);
            return oAuth2UserFactory.createCustomOAuth2User(user, oAuth2User);
        } catch (Exception e) {
            throw new OAuth2AuthenticationProcessingException(e);
        }
    }

    private Long extractDeveloperType(OAuth2UserRequest userRequest) {
        String developerTypeStr = userRequest.getAdditionalParameters().get("developerType").toString();
        if (developerTypeStr == null) {
            throw new IllegalArgumentException("Developer type is required");
        }
        return Long.parseLong(developerTypeStr);
    }

    private OAuth2Profile extractOAuth2Profile(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        try {
            OAuth2Provider provider = OAuth2Provider.valueOf(registrationId.toUpperCase());
            return OAuth2Extractor.extract(provider, oAuth2User.getAttributes());
        } catch (Exception e) {
            throw new OAuth2ProviderNotSupportedException(registrationId);
        }
    }
}
