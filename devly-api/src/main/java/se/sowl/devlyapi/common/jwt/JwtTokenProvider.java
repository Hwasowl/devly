package se.sowl.devlyapi.common.jwt;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;
import se.sowl.devlyapi.common.jwt.exception.ExpiredTokenException;
import se.sowl.devlyapi.common.jwt.exception.InvalidTokenException;
import se.sowl.devlyapi.oauth.dto.TokenResponse;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;
import se.sowl.devlydomain.user.domain.User;

import java.security.Key;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    public TokenResponse createToken(Authentication authentication) {
        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);
        return new TokenResponse(accessToken, refreshToken);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            throw new ExpiredTokenException("토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            throw new InvalidTokenException("지원하지 않는 토큰입니다.");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new InvalidTokenException("토큰이 비어있습니다.");
        }
    }

    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
            User user = getUserByClaims(claims);
            Map<String, Object> attributes = getAttributesByClaims(claims);

            Collection<? extends GrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
            DefaultOAuth2User oauth2User = new DefaultOAuth2User(authorities, attributes, "email");
            CustomOAuth2User principal = new CustomOAuth2User(user, oauth2User);

            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        } catch (Exception e) {
            log.error("Failed to create authentication: {}", e.getMessage());
            throw new InvalidTokenException("인증 정보를 생성할 수 없습니다.");
        }
    }

    private static Map<String, Object> getAttributesByClaims(Claims claims) {
        try {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("email", claims.get("email", String.class));
            attributes.put("name", claims.get("name", String.class));
            attributes.put("provider", claims.get("provider", String.class));
            return attributes;
        } catch (Exception e) {
            log.error("Failed to extract attributes from claims: {}", e.getMessage());
            throw new InvalidTokenException("토큰에서 사용자 정보를 추출할 수 없습니다.");
        }
    }

    private static User getUserByClaims(Claims claims) {
        return User.builder()
            .id(Long.parseLong(claims.getSubject()))
            .email(claims.get("email", String.class))
            .name(claims.get("name", String.class))
            .provider(claims.get("provider", String.class))
            .build();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String createAccessToken(CustomOAuth2User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getAccessTokenValidityInSeconds() * 1000);

        return Jwts.builder()
            .setSubject(user.getUserId().toString())
            .claim("email", user.getAttributes().get("email"))
            .claim("name", user.getAttributes().get("name"))
            .claim("provider", user.getAttributes().get("provider"))
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(getSigningKey())
            .compact();
    }

    private String createRefreshToken(CustomOAuth2User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getRefreshTokenValidityInSeconds() * 1000);

        return Jwts.builder()
            .setSubject(user.getUserId().toString())
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(getSigningKey())
            .compact();
    }
}
