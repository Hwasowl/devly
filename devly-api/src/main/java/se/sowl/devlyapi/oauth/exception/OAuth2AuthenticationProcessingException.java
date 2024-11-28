package se.sowl.devlyapi.oauth.exception;

public class OAuth2AuthenticationProcessingException extends RuntimeException {
    public OAuth2AuthenticationProcessingException(Throwable cause) {
        super("소셜 유저를 불러오는 도중 문제가 생겼습니다: ", cause);
    }
}
