package se.sowl.devlyapi.discussion.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
    @NotNull(message = "Round number는 필수입니다")
    private Integer roundNumber;
    
    @NotBlank(message = "Answer는 필수입니다")
    private String answer;
}