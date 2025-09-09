package se.sowl.devlyexternal.client.gpt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordGPTResponse {
    private String word;
    private String pronunciation;
    private String meaning;
    private ExampleDto example;
    private QuizDto quiz;
}