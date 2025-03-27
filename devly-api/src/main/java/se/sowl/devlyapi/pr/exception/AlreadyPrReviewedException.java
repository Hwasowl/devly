package se.sowl.devlyapi.pr.exception;

public class AlreadyPrReviewedException extends RuntimeException {
    public AlreadyPrReviewedException(String message) {
        super(message);
    }
}
