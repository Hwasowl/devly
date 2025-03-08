package se.sowl.devlyapi.pr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.sowl.devlydomain.pr.domain.PrComment;

import java.util.List;

@Getter
@AllArgsConstructor
public class PrCommentsResponse {
    private List<PrCommentResponse> comments;

    public static PrCommentsResponse from(List<PrComment> comments) {
        return new PrCommentsResponse(comments.stream().map(PrCommentResponse::from).toList());
    }
}
