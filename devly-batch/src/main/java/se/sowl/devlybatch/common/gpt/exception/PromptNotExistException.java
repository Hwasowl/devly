package se.sowl.devlybatch.common.gpt.exception;

public class PromptNotExistException extends RuntimeException {
    public PromptNotExistException(String message) {
        super(message);
    }
}
