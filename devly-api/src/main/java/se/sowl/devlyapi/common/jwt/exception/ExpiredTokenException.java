package se.sowl.devlyapi.common.jwt.exception;

public class ExpiredTokenException extends JwtException {
    public ExpiredTokenException(String message) {
        super(message);
    }
}
