package se.sowl.devlyapi.oauth.service;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlydomain.oauth.domain.OAuth2Provider;
import se.sowl.devlydomain.user.domain.User;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OAuthServiceTest extends MediumTest {

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("이미 가입된 구글 유저인 경우 유저 정보를 응답해야 한다.")
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

        // then
        assertThat(result).isNotNull();
        Map<String, Object> resultAttributes = result.getAttributes();
        assertThat(resultAttributes.get("name")).isEqualTo(name);
        assertThat(resultAttributes.get("email")).isEqualTo(email);
        assertThat(resultAttributes.get("provider")).isEqualTo(provider);
    }

    @Test
    @DisplayName("이미 가입된 카카오 유저인 경우 유저 정보를 응답해야 한다.")
    @Transactional
    public void loadExistKakaoUser() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        String provider = OAuth2Provider.KAKAO.getRegistrationId();
        String email = "hwasowl598@kakao.com";
        String name = "박정수";
        User user = createUser(2L, 1L, "박정수", "화솔", "hwasowl598@kakao.com", provider);
        userRepository.save(user);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "dummy-access-token", null, null);
        ClientRegistration clientRegistration = createClientRegistration(provider);
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        Map<String, Object> attributes = getKakaoAttribute(name, email);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);

        // when
        OAuth2User result = oAuthService.loadUser(userRequest);

        // then
        assertThat(result).isNotNull();
        Map<String, Object> resultAttributes = result.getAttributes();
        assertThat(resultAttributes.get("name")).isEqualTo(name);
        assertThat(resultAttributes.get("email")).isEqualTo(email);
        assertThat(resultAttributes.get("provider")).isEqualTo(provider);
    }

    @Test
    @DisplayName("이미 가입된 네이버 유저인 경우 유저 정보를 응답해야 한다.")
    @Transactional
    public void loadExistNaverUser() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        String provider = OAuth2Provider.NAVER.getRegistrationId();
        String email = "hwasowl598@naver.com";
        String name = "박정수";
        User user = createUser(2L, 1L, name, "화솔", email, provider);
        userRepository.save(user);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "dummy-access-token", null, null);
        ClientRegistration clientRegistration = createClientRegistration(provider);
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);
        Map<String, Object> attributes = getNaverAttribute(name, email);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);

        // when
        OAuth2User result = oAuthService.loadUser(userRequest);

        // then
        assertThat(result).isNotNull();
        Map<String, Object> resultAttributes = result.getAttributes();
        assertThat(resultAttributes.get("name")).isEqualTo(name);
        assertThat(resultAttributes.get("email")).isEqualTo(email);
        assertThat(resultAttributes.get("provider")).isEqualTo(provider);
    }

    @Test
    @DisplayName("가입되지 않은 구글 유저가 인증한 경우 회원가입 후 유저 정보를 응답해야 한다.")
    @Transactional
    public void loadNotExistGoogleUser() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        String provider = OAuth2Provider.GOOGLE.getRegistrationId();
        String name = "박정수";
        String email = "hwasowl598@gmail.com";

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "dummy-access-token", null, null);
        ClientRegistration clientRegistration = createClientRegistration(provider);
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        Map<String, Object> attributes = getGoogleAttribute(name, email);

        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);

        // when
        OAuth2User result = oAuthService.loadUser(userRequest);

        // then
        assertThat(result).isNotNull();
        Map<String, Object> resultAttributes = result.getAttributes();
        assertThat(resultAttributes.get("name")).isEqualTo(name);
        assertThat(resultAttributes.get("email")).isEqualTo(email);
        assertThat(resultAttributes.get("provider")).isEqualTo(provider);
    }

    @Test
    @DisplayName("가입되지 않은 카카오 유저가 인증한 경우 회원가입 후 유저 정보를 응답해야 한다.")
    void loadNotExistKaKaoUser() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        String provider = OAuth2Provider.KAKAO.getRegistrationId();
        String email = "hwasowl598@kakao.com";
        String name = "박정수";

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "dummy-access-token", null, null);
        ClientRegistration clientRegistration = createClientRegistration(provider);
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);

        Map<String, Object> attributes = getKakaoAttribute(name, email);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);

        // when
        OAuth2User result = oAuthService.loadUser(userRequest);

        // then
        assertThat(result).isNotNull();
        Map<String, Object> resultAttributes = result.getAttributes();
        assertThat(resultAttributes.get("name")).isEqualTo(name);
        assertThat(resultAttributes.get("email")).isEqualTo(email);
        assertThat(resultAttributes.get("provider")).isEqualTo(provider);
    }

    @Test
    @DisplayName("가입하지 않은 네이버 유저가 인증한 경우 유저 정보를 응답해야 한다.")
    @Transactional
    public void loadNotExistNaverUser() {
        // given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        String provider = OAuth2Provider.NAVER.getRegistrationId();
        String email = "hwasowl598@naver.com";
        String name = "박정수";

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "dummy-access-token", null, null);
        ClientRegistration clientRegistration = createClientRegistration(provider);
        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken);
        Map<String, Object> attributes = getNaverAttribute(name, email);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);

        // when
        OAuth2User result = oAuthService.loadUser(userRequest);

        // then
        assertThat(result).isNotNull();
        Map<String, Object> resultAttributes = result.getAttributes();
        assertThat(resultAttributes.get("name")).isEqualTo(name);
        assertThat(resultAttributes.get("email")).isEqualTo(email);
        assertThat(resultAttributes.get("provider")).isEqualTo(provider);
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

    private Map<String, Object> getKakaoAttribute(String name, String email) {
        Map<String, Object> kakaoAccount = Map.of(
            "profile", Map.of(
                "nickname", name,
                "thumbnail_image_url", "http://t1.kakaocdn.net/account_images/default_profile.jpeg.twg.thumb.R110x110",
                "profile_image_url", "http://t1.kakaocdn.net/account_images/default_profile.jpeg.twg.thumb.R640x640"
            ),
            "email", email
        );
        return Map.of(
            "id", "3481867707",
            "connected_at", "2024-05-14T10:23:03Z",
            "kakao_account", kakaoAccount
        );
    }

    private Map<String, Object> getNaverAttribute(String name, String email) {
        return Map.of(
            "response", Map.of(
                "id", "2P_mzBBdsMUqMAcQQIWdmLM123v2-LVK9yDU2erw1237crNws",
                "profile_image", "https://ssl.pstatic.net/static/pwe/address/img_profile.png",
                "name", name,
                "email", email
            )
        );

    }
}
