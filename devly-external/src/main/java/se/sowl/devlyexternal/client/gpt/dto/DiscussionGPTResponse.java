package se.sowl.devlyexternal.client.gpt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscussionGPTResponse {
    private String feedback;
    private Double score;
    
    @JsonProperty("nextQuestion")
    private String nextQuestion;
    
    @JsonProperty("finalEvaluation")
    private FinalEvaluation finalEvaluation;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FinalEvaluation {
        @JsonProperty("overallScore")
        private Double overallScore;
        
        private List<String> strengths;
        private List<String> improvements;
        private String summary;
    }
    
    public boolean isFinalRound() {
        return finalEvaluation != null;
    }
}