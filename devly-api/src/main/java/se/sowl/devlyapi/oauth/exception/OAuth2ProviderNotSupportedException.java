package se.sowl.devlyapi.oauth.exception;

public class OAuth2ProviderNotSupportedException extends IllegalArgumentException {
    public OAuth2ProviderNotSupportedException(String provider) {
        super("지원하지 않는 OAuth2 제공자: " + provider);
    }
}
