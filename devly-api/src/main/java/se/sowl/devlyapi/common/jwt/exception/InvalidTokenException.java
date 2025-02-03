package se.sowl.devlyapi.common.jwt.exception;

public class InvalidTokenException extends JwtException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
