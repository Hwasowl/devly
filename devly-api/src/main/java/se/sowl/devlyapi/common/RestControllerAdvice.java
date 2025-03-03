package se.sowl.devlyapi.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import se.sowl.devlyapi.common.jwt.exception.ExpiredTokenException;
import se.sowl.devlyapi.common.jwt.exception.InvalidTokenException;
import se.sowl.devlyapi.oauth.exception.OAuth2AuthenticationProcessingException;
import se.sowl.devlyapi.oauth.exception.OAuth2ProviderNotSupportedException;

@Slf4j
@ResponseBody
@ControllerAdvice
public class RestControllerAdvice {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<Void> handleBadRequest(Exception e) {
        log.error("BadRequest Exception", e);
        return CommonResponse.fail(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CommonResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("RuntimeException", e);
        return CommonResponse.fail("서버에 문제가 생겼어요. 잠시 후 다시 시도해주세요.");
    }

    @ExceptionHandler(OAuth2ProviderNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResponse<Void> handleOAuth2ProviderNotSupportedException(OAuth2ProviderNotSupportedException e) {
        log.error("OAuth2 Provider Not Supported", e);
        return CommonResponse.fail("지원하지 않는 로그인 방식이에요.");
    }

    @ExceptionHandler(OAuth2AuthenticationProcessingException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CommonResponse<Void> handleOAuth2AuthenticationProcessingException(OAuth2AuthenticationProcessingException e) {
        log.error("OAuth2 Authentication Processing Error", e);
        return CommonResponse.fail("인증 처리 도중 문제가 발생했어요. 잠시 후 다시 시도해주세요.");
    }

    @ExceptionHandler({InvalidTokenException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CommonResponse<Void> handleInvalidJwtTokenException(InvalidTokenException e) {
        log.error("Invalid JWT Token Error", e);
        return CommonResponse.fail("사용자 인증에 실패했어요. 잠시 후 다시 시도해주세요.");
    }

    @ExceptionHandler({ExpiredTokenException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CommonResponse<Void> handleOAuth2AuthenticationException(ExpiredTokenException e) {
        log.error("Expired Jwt Token", e);
        return CommonResponse.fail("로그인 시간이 만료되었어요. 다시 로그인 해주세요.");
    }
}
