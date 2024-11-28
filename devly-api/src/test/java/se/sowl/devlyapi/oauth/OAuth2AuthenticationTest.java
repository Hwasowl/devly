package se.sowl.devlyapi.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.devlyapi.oauth.service.OAuthService;
import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("지원하지 않는 OAuth2 제공자로 요청시 500 에러를 반환한다")
    void whenRequestUnsupportedProvider_thenReturn500() throws Exception {
        // given
        when(clientRegistrationRepository.findByRegistrationId("unsupported"))
            .thenReturn(null);

        // when & then
        mockMvc.perform(get("/oauth2/authorization/unsupported"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(result -> {
                assertThat(result.getResponse().getErrorMessage()).contains("Internal Server Error");
            });
    }

    @Test
    @DisplayName("OAuth2 인증 실패시 401 에러를 반환한다")
    void whenOAuth2AuthenticationFails_thenReturn401() throws Exception {
        // given
        OAuth2Error oauth2Error = new OAuth2Error("authentication_error");
        OAuth2AuthenticationException authException = new OAuth2AuthenticationException(oauth2Error);

        when(oAuthService.loadUser(any())).thenThrow(authException);

        // when & then
        mockMvc.perform(get("/login/oauth2/code/google")
                .param("code", "test-auth-code")
                .param("state", "test-state"))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("FAIL"))
            .andExpect(jsonPath("$.message").value("인증에 실패했어요. 잠시 후 다시 시도해주세요."))
            .andExpect(jsonPath("$.result").isEmpty());
    }
}
