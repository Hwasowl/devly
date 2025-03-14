package se.sowl.devlyapi.pr.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.devlyapi.common.CommonResponse;
import se.sowl.devlyapi.pr.dto.files.PrChangedFilesResponse;
import se.sowl.devlyapi.pr.dto.comments.PrCommentsResponse;
import se.sowl.devlyapi.pr.dto.PrResponse;
import se.sowl.devlyapi.pr.service.PrService;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;

@RestController
@RequestMapping("/api/pr")
@RequiredArgsConstructor
public class PrController {

    private final PrService prService;

    @GetMapping("/{studyId}")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<PrResponse> getWords(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId) {
        PrResponse response = prService.getPr(customOAuth2User.getUserId(), studyId);
        return CommonResponse.ok(response);
    }

    @GetMapping("/changed-files/{prId}")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<PrChangedFilesResponse> getChangedFiles(@PathVariable Long prId) {
        PrChangedFilesResponse response = prService.getChangedFiles(prId);
        return CommonResponse.ok(response);
    }

    @GetMapping("/comments/{prId}")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<PrCommentsResponse> getComments(@PathVariable Long prId) {
        PrCommentsResponse response = prService.getComments(prId);
        return CommonResponse.ok(response);
    }
}
