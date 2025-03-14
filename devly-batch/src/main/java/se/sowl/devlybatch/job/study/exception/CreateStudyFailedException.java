package se.sowl.devlybatch.job.study.exception;

public class CreateStudyFailedException extends RuntimeException {
    public CreateStudyFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
