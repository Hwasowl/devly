package se.sowl.devlyexternal.client.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GPTResponse {
    private final String content;  // GPT 응답 내용만 필요
}
