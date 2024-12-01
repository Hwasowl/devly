package se.sowl.devlyapi.oauth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.web.servlet.MockMvc;
import se.sowl.devlyapi.oauth.service.OAuthService;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
class OAuth2AuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuthService oAuthService;

    @Test
    @DisplayName("OAuth2 로그인 요청시 인증 제공자의 로그인 페이지로 리다이렉트된다")
    void oauth2LoginRedirectTest() throws Exception {
        // given
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
            .clientId("client-id")
            .clientSecret("client-secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .tokenUri("https://www.googleapis.com/oauth2/v4/token")
            .build();

        when(clientRegistrationRepository.findByRegistrationId("google")).thenReturn(clientRegistration);

        // when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/oauth2/authorization/{registrationId}", "google"))
            .andExpect(status().is3xxRedirection())
            .andDo(document("oauth2-login",
                pathParameters(
                    parameterWithName("registrationId").description("인증 제공자 ID")
                )
            ));
    }
}
