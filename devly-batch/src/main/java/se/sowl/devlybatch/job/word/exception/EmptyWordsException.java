package se.sowl.devlybatch.job.word.exception;

public class EmptyWordsException extends RuntimeException{
    public EmptyWordsException(String message) {
        super(message);
    }
}
