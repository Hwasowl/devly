package se.sowl.devlybatch.job.word.exception;

public class WordCreationException extends RuntimeException {
    public WordCreationException(String message, Exception error) {
        super(message, error);
    }
}
