package se.sowl.devlyapi.pr.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.sowl.devlyapi.common.CommonResponse;
import se.sowl.devlyapi.pr.dto.review.PrCommentReviewRequest;
import se.sowl.devlyapi.pr.dto.files.PrChangedFilesResponse;
import se.sowl.devlyapi.pr.dto.comments.PrCommentsResponse;
import se.sowl.devlyapi.pr.dto.PrResponse;
import se.sowl.devlyapi.pr.dto.review.PrCommentReviewResponse;
import se.sowl.devlyapi.pr.service.PrChangedFilesService;
import se.sowl.devlyapi.pr.service.PrCommentService;
import se.sowl.devlyapi.pr.service.PrReviewService;
import se.sowl.devlyapi.pr.service.PrService;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;

@RestController
@RequestMapping("/api/pr")
@RequiredArgsConstructor
public class PrController {

    private final PrService prService;
    private final PrCommentService prCommentService;
    private final PrChangedFilesService prChangedFilesService;
    private final PrReviewService prReviewService;

    @GetMapping("/{studyId}")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<PrResponse> getPr(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId) {
        PrResponse response = prService.getPrResponse(customOAuth2User.getUserId(), studyId);
        return CommonResponse.ok(response);
    }

    @GetMapping("/{prId}/changed-files")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<PrChangedFilesResponse> getChangedFiles(@PathVariable Long prId) {
        PrChangedFilesResponse response = prChangedFilesService.getChangedFilesResponse(prId);
        return CommonResponse.ok(response);
    }

    @GetMapping("/{prId}/comments")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<PrCommentsResponse> getComments(@PathVariable Long prId) {
        PrCommentsResponse response = prCommentService.getCommentsResponse(prId);
        return CommonResponse.ok(response);
    }

    @PostMapping("/review/comment/{prCommentId}")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<PrCommentReviewResponse> reviewPrComment(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long prCommentId, @RequestBody PrCommentReviewRequest request) {
        PrCommentReviewResponse response = prReviewService.reviewPrComment(customOAuth2User.getUserId(), prCommentId, request.getStudyId(), request.getAnswer());
        return CommonResponse.ok(response);
    }
}
