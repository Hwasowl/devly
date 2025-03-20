package se.sowl.devlyexternal.common.gpt.exception;

public class PromptNotExistException extends RuntimeException {
    public PromptNotExistException(String message) {
        super(message);
    }
}
