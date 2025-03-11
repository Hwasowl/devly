package se.sowl.devlyapi.pr.dto.comments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.sowl.devlydomain.pr.domain.PrComment;

@Getter
@AllArgsConstructor
public class PrCommentResponse {
    private Long id;
    private Long idx;
    private Long prId;
    private String content;

    public static PrCommentResponse from(PrComment comment) {
        return new PrCommentResponse(comment.getId(), comment.getIdx(), comment.getPrId(), comment.getContent());
    }
}
