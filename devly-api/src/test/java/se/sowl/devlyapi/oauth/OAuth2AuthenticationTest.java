package se.sowl.devlyapi.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.devlyapi.oauth.service.OAuthService;
import se.sowl.devlyapi.oauth.exception.OAuth2AuthenticationProcessingException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class OAuth2AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuthService oAuthService;

    @Value("${spring.front.url}")
    private String frontUrl;

    @Test
    @DisplayName("OAuth2 로그인 요청시 인증 제공자의 로그인 페이지로 리다이렉트된다")
    void whenRequestOAuth2Login_thenRedirectToProvider() throws Exception {
        // given
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .clientId("client-id")
            .clientSecret("client-secret")
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://www.googleapis.com/oauth2/v4/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName("sub")
            .clientName("Google")
            .build();

        when(clientRegistrationRepository.findByRegistrationId("google"))
            .thenReturn(clientRegistration);

        // when & then
        mockMvc.perform(get("/oauth2/authorization/google"))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", containsString("https://accounts.google.com/o/oauth2/v2/auth")));
    }

    @Test
    @DisplayName("지원하지 않는 OAuth2 제공자로 요청시 400 에러를 반환한다")
    void whenRequestUnsupportedProvider_thenReturn400() throws Exception {
        // given
        String provider = "unsupported";
        when(clientRegistrationRepository.findByRegistrationId("provider"))
            .thenReturn(null);

        // when & then
        mockMvc.perform(get("/oauth2/authorization/unsupported"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("지원하지 않는 OAuth2 제공자: " + provider));
    }

    @Test
    @DisplayName("OAuth2 콜백 처리 시 인증 실패하면 401 에러를 반환한다")
    void whenOAuth2CallbackFails_thenReturn401() throws Exception {
        // given
        when(oAuthService.loadUser(any()))
            .thenThrow(new OAuth2AuthenticationProcessingException(new RuntimeException("Authentication failed")));

        // when & then
        mockMvc.perform(get("/login/oauth2/code/google")
                .param("code", "test-auth-code")
                .param("state", "test-state"))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("인증 처리 도중 문제가 발생했어요. 잠시 후 다시 시도해주세요."));
    }

    @Test
    @DisplayName("OAuth2 인증 성공시 프론트엔드 URL로 리다이렉트된다")
    void whenOAuth2LoginSuccess_thenRedirectToFrontend() throws Exception {
        // given
        OAuth2User mockUser = mock(OAuth2User.class);
        when(oAuthService.loadUser(any())).thenReturn(mockUser);

        // when & then
        mockMvc.perform(get("/login/oauth2/code/google")
                .param("code", "test-auth-code")
                .param("state", "test-state"))
            .andDo(print())
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(frontUrl));
    }
}
