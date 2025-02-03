package se.sowl.devlyapi.common.oauth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import se.sowl.devlyapi.common.jwt.JwtTokenProvider;
import se.sowl.devlyapi.oauth.dto.TokenResponse;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtTokenProvider tokenProvider;

    @Value("${front.url}")
    private String frontUrl;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        TokenResponse tokenResponse = tokenProvider.createToken(authentication);
        ResponseCookie cookie = ResponseCookie.from("accessToken", tokenResponse.getAccessToken())
            .httpOnly(true).secure(true).sameSite("Lax").path("/").maxAge(Duration.ofHours(6)).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        getRedirectStrategy().sendRedirect(request, response, frontUrl);
    }
}
