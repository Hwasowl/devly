package se.sowl.devlyapi.pr.dto.comments;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PrCommentReviewRequest {
    private String answer;
    private Long studyId;
}
