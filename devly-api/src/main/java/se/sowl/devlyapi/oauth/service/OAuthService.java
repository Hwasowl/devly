package se.sowl.devlyapi.oauth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.sowl.devlyapi.oauth.exception.OAuth2AuthenticationProcessingException;
import se.sowl.devlyapi.oauth.exception.OAuth2ProviderNotSupportedException;
import se.sowl.devlyapi.oauth.factory.OAuth2UserFactory;
import se.sowl.devlyapi.user.service.UserService;
import se.sowl.devlydomain.oauth.domain.OAuth2Extractor;
import se.sowl.devlydomain.oauth.domain.OAuth2Profile;
import se.sowl.devlydomain.oauth.domain.OAuth2Provider;
import se.sowl.devlydomain.user.domain.User;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final DefaultOAuth2UserService defaultOAuth2UserService;
    private final OAuth2UserFactory oAuth2UserFactory;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        try {
            OAuth2User loadedUser = defaultOAuth2UserService.loadUser(userRequest);
            OAuth2Profile profile = extractOAuth2Profile(userRequest, loadedUser);
            String state = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getParameter("state");
            Long developerType = extractDeveloperType(state);
            User user = userService.getOrCreateUser(profile, developerType);
            OAuth2User oAuth2User = oAuth2UserFactory.createOAuth2User(userRequest, loadedUser, profile);
            return oAuth2UserFactory.createCustomOAuth2User(user, oAuth2User);
        } catch (Exception e) {
            throw new OAuth2AuthenticationProcessingException(e);
        }
    }

    private Long extractDeveloperType(String encodedState) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedState);
            Map<String, String> stateData = objectMapper.readValue(
                new String(decodedBytes, StandardCharsets.UTF_8),
                new TypeReference<Map<String, String>>() {}
            );
            return Long.parseLong(stateData.get("developerType"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid developer type in state", e);
        }
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
