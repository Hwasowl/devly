package se.sowl.devlyexternal.client.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GPTResponse {
    private List<Choice> choices;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private Message message;
        private String finishReason;
        private int index;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    public String getContent() {
        if (choices != null && !choices.isEmpty() && choices.getFirst().getMessage() != null) {
            return choices.getFirst().getMessage().getContent();
        }
        return null;
    }
}
