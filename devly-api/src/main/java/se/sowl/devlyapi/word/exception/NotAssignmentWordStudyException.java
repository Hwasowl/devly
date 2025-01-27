package se.sowl.devlyapi.word.exception;

public class NotAssignmentWordStudyException extends RuntimeException {
    public NotAssignmentWordStudyException() {
        super("아직 학습할 수 없습니다.");
    }
}
