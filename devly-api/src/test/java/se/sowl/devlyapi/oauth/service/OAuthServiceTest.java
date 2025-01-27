package se.sowl.devlyapi.oauth.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.common.jwt.JwtTokenProvider;
import se.sowl.devlyapi.oauth.dto.TokenResponse;
import se.sowl.devlydomain.oauth.domain.OAuth2Provider;
import se.sowl.devlydomain.user.domain.User;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class OAuthServiceTest extends MediumTest {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("이미 가입된 구글 유저인 경우 유저 정보와 JWT 토큰을 응답해야 한다.")
    @Transactional
    public void loadExistGoogleUser() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        String provider = OAuth2Provider.GOOGLE.getRegistrationId();
        String email = "hwasowl598@gmail.com";
        String name = "박정수";
        User user = createUser(1L, 1L, name, "화솔", email, provider);
        userRepository.save(user);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "dummy-access-token", null, null);
        ClientRegistration clientRegistration = createClientRegistration(provider);
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
        User authenticatedUser = (User) jwtAuthentication.getPrincipal();
        assertThat(authenticatedUser.getEmail()).isEqualTo(email);
        assertThat(authenticatedUser.getName()).isEqualTo(name);
        assertThat(authenticatedUser.getProvider()).isEqualTo(provider);
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
