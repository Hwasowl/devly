package se.sowl.devlydomain.oauth.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2Profile {
    private String name;
    private String email;
    private String provider;
}
