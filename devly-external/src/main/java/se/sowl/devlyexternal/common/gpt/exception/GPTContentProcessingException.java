package se.sowl.devlyexternal.common.gpt.exception;

public class GPTContentProcessingException extends RuntimeException {
    public GPTContentProcessingException(String message) {
        super(message);
    }

    public GPTContentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
