package se.sowl.devlyexternal.client.gpt.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GPTRequest {
    private String model;
    private List<Message> messages;
    private double temperature;

    @Getter
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
