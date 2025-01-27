package se.sowl.devlydomain.oauth.domain;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
public enum OAuth2Extractor {
    GOOGLE(OAuth2Provider.GOOGLE, OAuth2Extractor::extractGoogleProfile);

    private final OAuth2Provider provider;
    private final Function<Map<String, Object>, OAuth2Profile> extractor;

    public static OAuth2Profile extract(OAuth2Provider provider, Map<String, Object> attributes) {
        return Arrays.stream(values())
            .filter(extractor -> extractor.provider == provider)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("올바르지 않은 제공자: " + provider))
            .extractor.apply(attributes);
    }

    private static OAuth2Profile extractGoogleProfile(Map<String, Object> attributes) {
        return OAuth2Profile.builder()
            .name((String) attributes.get("name"))
            .email((String) attributes.get("email"))
            .provider(OAuth2Provider.GOOGLE.getRegistrationId())
            .build();
    }
}

