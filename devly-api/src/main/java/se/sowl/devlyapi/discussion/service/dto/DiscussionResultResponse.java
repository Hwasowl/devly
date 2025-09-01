package se.sowl.devlyapi.discussion.service.dto;

import lombok.Builder;
import lombok.Getter;
import se.sowl.devlydomain.discussion.domain.Discussion;
import se.sowl.devlydomain.discussion.domain.DiscussionRound;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class DiscussionResultResponse {
    private final Long discussionId;
    private final String topic;
    private final List<RoundResult> rounds;
    private final Double overallScore;
    private final String finalFeedback;
    
    @Getter
    @Builder
    public static class RoundResult {
        private final Integer roundNumber;
        private final String question;
        private final String answer;
        private final String feedback;
        private final Double score;
        
        public static RoundResult from(DiscussionRound round) {
            return RoundResult.builder()
                .roundNumber(round.getRoundNumber())
                .question(round.getQuestion())
                .answer(round.getUserAnswer())
                .feedback(round.getGptFeedback())
                .score(round.getRoundScore())
                .build();
        }
    }
    
    public static DiscussionResultResponse from(Discussion discussion, List<DiscussionRound> rounds) {
        return DiscussionResultResponse.builder()
            .discussionId(discussion.getId())
            .topic(discussion.getTopic().getTitle())
            .rounds(rounds.stream()
                .map(RoundResult::from)
                .collect(Collectors.toList()))
            .overallScore(discussion.getOverallScore())
            .finalFeedback(discussion.getFinalFeedback())
            .build();
    }
}