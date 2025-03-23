package se.sowl.devlyapi.pr.exception;

public class PrCommentNotExistException extends RuntimeException {
    public PrCommentNotExistException(String message) {
        super(message);
    }
}
