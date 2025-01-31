package se.sowl.devlyapi.study.exception;

public class DuplicateInitialUserStudiesException extends RuntimeException {
    public DuplicateInitialUserStudiesException(String message) {
        super(message);
    }
}
