package se.sowl.devlyapi.common.jwt.exception;

public class JwtException extends RuntimeException {
    public JwtException(String message) {
        super(message);
    }
}
