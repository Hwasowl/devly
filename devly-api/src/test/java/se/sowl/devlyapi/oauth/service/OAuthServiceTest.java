package se.sowl.devlyapi.oauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.oauth.dto.TokenResponse;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.oauth.domain.OAuth2Provider;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class OAuthServiceTest extends MediumTest {

    @Test
    @DisplayName("이미 가입된 구글 유저인 경우 유저 정보와 JWT 토큰을 응답해야 한다.")
    @Transactional
    public void shouldReturnUserInfoAndTokenForExistingGoogleUser() throws JsonProcessingException {
        // given
        List<DeveloperType> developerTypes = developerTypeRepository.saveAll(getDeveloperTypes());
        DeveloperType backendDeveloperType = developerTypes.stream()
            .filter(type -> type.getName().equals("Backend Developer"))
            .findFirst()
            .orElseThrow();

        String provider = OAuth2Provider.GOOGLE.getRegistrationId();
        String email = "hwasowl598@gmail.com";
        String name = "박정수";

        userRepository.save(User.builder()
            .id(1L)
            .developerType(backendDeveloperType)
            .name(name)
            .nickname("화솔")
            .email(email)
            .provider(provider)
            .build());

        OAuth2User oAuth2User = mock(OAuth2User.class);
        ClientRegistration clientRegistration = createClientRegistration(provider);

        Map<String, String> stateData = new HashMap<>();
        stateData.put("developerType", String.valueOf(backendDeveloperType.getId()));
        stateData.put("originalState", "random-state");

        String encodedState = Base64.getUrlEncoder().encodeToString(
            objectMapper.writeValueAsString(stateData).getBytes(StandardCharsets.UTF_8)
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("state", encodedState);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "accessToken",
            null,
            null
        );
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        Map<String, Object> attributes = getGoogleAttribute(name, email);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);

        // when
        OAuth2User result = oAuthService.loadUser(userRequest);
        TokenResponse tokenResponse = jwtTokenProvider.createToken(
            new UsernamePasswordAuthenticationToken(result, null, result.getAuthorities())
        );

        // then
        assertThat(result).isNotNull();
        Map<String, Object> resultAttributes = result.getAttributes();
        assertThat(resultAttributes.get("name")).isEqualTo(name);
        assertThat(resultAttributes.get("email")).isEqualTo(email);
        assertThat(resultAttributes.get("provider")).isEqualTo(provider);

        assertThat(tokenResponse.getAccessToken()).isNotNull();
        assertThat(tokenResponse.getRefreshToken()).isNotNull();
        assertThat(tokenResponse.getTokenType()).isEqualTo("Bearer");
        assertThat(jwtTokenProvider.validateToken(tokenResponse.getAccessToken())).isTrue();

        Authentication jwtAuthentication = jwtTokenProvider.getAuthentication(tokenResponse.getAccessToken());
        CustomOAuth2User authenticatedUser = (CustomOAuth2User) jwtAuthentication.getPrincipal();

        User userInfo = authenticatedUser.getUser();
        assertThat(userInfo.getEmail()).isEqualTo(email);
        assertThat(userInfo.getName()).isEqualTo(name);
        assertThat(userInfo.getProvider()).isEqualTo(provider);

        assertThat(authenticatedUser.getAttributes())
            .containsEntry("email", email)
            .containsEntry("name", name)
            .containsEntry("provider", provider);

        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("신규 구글 유저 가입시 유저 학습이 타입에 맞게 모두 생성되어야 한다.")
    @Transactional
    public void shouldCreateUserStudiesForNewGoogleUser() throws JsonProcessingException {
        // given
        List<DeveloperType> developerTypes = developerTypeRepository.saveAll(getDeveloperTypes());
        DeveloperType backendDeveloperType = developerTypes.stream()
            .filter(type -> type.getName().equals("Backend Developer"))
            .findFirst()
            .orElseThrow();

        List<StudyType> studyTypes = studyTypeRepository.saveAll(getStudyTypes());

        for (StudyType studyType : studyTypes) {
            studyRepository.save(Study.builder()
                .studyType(studyType)
                .developerType(backendDeveloperType)
                .build());
        }

        String provider = OAuth2Provider.GOOGLE.getRegistrationId();
        String email = "hwasowl598@gmail.com";
        String name = "박정수";

        OAuth2User oAuth2User = mock(OAuth2User.class);
        ClientRegistration clientRegistration = createClientRegistration(provider);

        Map<String, String> stateData = new HashMap<>();
        stateData.put("developerType", String.valueOf(backendDeveloperType.getId()));
        stateData.put("originalState", "random-state");

        String encodedState = Base64.getUrlEncoder().encodeToString(
            objectMapper.writeValueAsString(stateData).getBytes(StandardCharsets.UTF_8)
        );

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("state", encodedState);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "accessToken",
            null,
            null
        );
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        Map<String, Object> attributes = getGoogleAttribute(name, email);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);

        // when
        CustomOAuth2User loadedUser = (CustomOAuth2User) oAuthService.loadUser(userRequest);

        // then
        List<UserStudy> userStudies = userStudyRepository.findAllByUserId(loadedUser.getUserId());
        assertThat(userStudies.size()).isEqualTo(StudyTypeEnum.values().length);

        for (UserStudy userStudy : userStudies) {
            assertThat(userStudy.getUser()).isEqualTo(loadedUser.getUser());
            assertThat(userStudy.getStudy()).isNotNull();
        }

        RequestContextHolder.resetRequestAttributes();
    }

    private ClientRegistration createClientRegistration(String provider) {
        return ClientRegistration.withRegistrationId(provider)
            .clientId("clientId")
            .clientSecret("clientSecret")
            .scope("email")
            .authorizationUri("https://test/accounts.google.com/o/oauth2/auth")
            .tokenUri("https://test/oauth2.googleapis.com/token")
            .userInfoUri("https://test/www.googleapis.com/oauth2/v3/userinfo")
            .redirectUri("https://test/www.googleapis.com/oauth2/google/redirect")
            .userNameAttributeName("sub")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .clientName(provider)
            .build();
    }

    private Map<String, Object> getGoogleAttribute(String name, String email) {
        return Map.of("sub", "1234567890", "email", email, "name", name);
    }
}
