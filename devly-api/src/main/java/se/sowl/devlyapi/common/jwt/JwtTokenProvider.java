package se.sowl.devlyapi.common.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import se.sowl.devlyapi.oauth.dto.TokenResponse;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;
import se.sowl.devlydomain.user.domain.User;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    public TokenResponse createToken(Authentication authentication) {
        CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return new TokenResponse(accessToken, refreshToken);
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

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();

        Collection<? extends GrantedAuthority> authorities =
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        User principal = User.builder()
            .id(Long.parseLong(claims.getSubject()))
            .email(claims.get("email", String.class))
            .name(claims.get("name", String.class))
            .provider(claims.get("provider", String.class))
            .build();

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}
