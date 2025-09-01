package se.sowl.devlyapi.discussion.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoundResponse {
    private final String feedback;
    private final Double score;
    private final String nextQuestion;
    private final Integer currentRound;
    private final Boolean isCompleted;
}