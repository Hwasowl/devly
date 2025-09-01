package se.sowl.devlyapi.discussion.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiscussionStartResponse {
    private final Long discussionId;
    private final String topic;
    private final String description;
    private final String firstQuestion;
    private final Integer currentRound;
    private final Integer totalRounds;
}