package se.sowl.devlyapi.study.exception;

public class StudyNotExistException extends RuntimeException {
    public StudyNotExistException(String message) {
        super(message);
    }
}
