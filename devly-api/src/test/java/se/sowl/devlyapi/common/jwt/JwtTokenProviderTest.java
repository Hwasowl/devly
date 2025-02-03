package se.sowl.devlyapi.common.jwt;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import se.sowl.devlyapi.MediumTest;
import se.sowl.devlyapi.common.jwt.exception.ExpiredTokenException;
import se.sowl.devlyapi.common.jwt.exception.InvalidTokenException;
import se.sowl.devlyapi.oauth.dto.TokenResponse;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;
import se.sowl.devlydomain.user.domain.User;

import java.lang.reflect.Method;
import java.security.Key;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenProviderTest extends MediumTest {

    @Test
    @DisplayName("유효한 토큰으로 인증 정보를 생성한다")
    void createAuthenticationWithValidToken() {
        // given
        User user = createUser(1L, 1L, "박정수", "솔", "123@naver.com", "google");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", user.getEmail());
        attributes.put("name", user.getName());
        attributes.put("provider", user.getProvider());

        Collection<? extends GrantedAuthority> authorities =
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        OAuth2User oauth2User = new DefaultOAuth2User(authorities, attributes, "email");
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(user, oauth2User);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(customOAuth2User, "", authorities);

        // when
        TokenResponse tokenResponse = jwtTokenProvider.createToken(authentication);
        Authentication resultAuth = jwtTokenProvider.getAuthentication(tokenResponse.getAccessToken());
        CustomOAuth2User principal = (CustomOAuth2User) resultAuth.getPrincipal();

        // then
        assertThat(principal.getUserId()).isEqualTo(user.getId());
        assertThat(principal.getAttributes())
            .containsEntry("email", user.getEmail())
            .containsEntry("name", user.getName())
            .containsEntry("provider", user.getProvider());
    }

    @Test
    @DisplayName("만료된 토큰으로 예외가 발생한다")
    void validateExpiredToken() {
        // given
        String expiredToken = createExpiredToken();

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(expiredToken))
            .isInstanceOf(ExpiredTokenException.class)
            .hasMessage("토큰이 만료되었습니다.");
    }

    @Test
    @DisplayName("토큰 만료 시간이 되지 않았다면 검증에 성공한다")
    void validateNotExpiredToken() {
        // given
        Date now = new Date();
        Date futureTime = new Date(now.getTime() + 3600 * 1000);
        String validToken = createToken(futureTime);

        // when & then
        assertDoesNotThrow(() -> jwtTokenProvider.validateToken(validToken));
    }


    @Test
    @DisplayName("유효하지 않은 토큰으로 예외가 발생한다")
    void validateInvalidToken() {
        // given
        String invalidToken = "invalid_token";

        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(invalidToken))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessage("유효하지 않은 토큰입니다.");
    }

    @Test
    @DisplayName("토큰이 null이면 예외가 발생한다")
    void validateNullToken() {
        // when & then
        assertThatThrownBy(() -> jwtTokenProvider.validateToken(null))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessage("토큰이 비어있습니다.");
    }

    private Key getSigningKeyFromProvider() {
        try {
            Method method = JwtTokenProvider.class.getDeclaredMethod("getSigningKey");
            method.setAccessible(true);
            return (Key) method.invoke(jwtTokenProvider);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get signing key using reflection", e);
        }
    }

    private String createExpiredToken() {
        Date now = new Date();
        // 토큰 만료 시간을 1시간 전으로 설정
        Date validity = new Date(now.getTime() - 3600 * 1000);

        return Jwts.builder()
            .setSubject("1")
            .claim("email", "test@test.com")
            .claim("name", "Test User")
            .claim("provider", "google")
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(getSigningKeyFromProvider())
            .compact();
    }

    private String createToken(Date expiration) {
        Date now = new Date();

        return Jwts.builder()
            .setSubject("1")
            .claim("email", "test@test.com")
            .claim("name", "Test User")
            .claim("provider", "google")
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(getSigningKeyFromProvider())
            .compact();
    }
}
