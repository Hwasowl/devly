package se.sowl.devlyexternal.client.gpt.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class GPTResponse {
    private List<Choice> choices;

    @Getter
    public static class Choice {
        private GPTRequest.Message message;
        private String content;
    }
}
