package se.sowl.devlyapi.study.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.sowl.devlyapi.common.CommonResponse;
import se.sowl.devlyapi.study.dto.UpdateWordReviewRequest;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlyapi.study.dto.WordReviewResponse;
import se.sowl.devlyapi.study.service.StudyService;
import se.sowl.devlyapi.study.dto.CreateWordReviewRequest;
import se.sowl.devlyapi.word.service.WordService;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {
    private final StudyService studyService;
    private final WordService wordService;

    @GetMapping("/tasks")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<UserStudyTasksResponse> getUserStudyTasks(@AuthenticationPrincipal CustomOAuth2User user) {
        UserStudyTasksResponse response = studyService.getUserStudyTasks(user.getUserId());
        return CommonResponse.ok(response);
    }

    @GetMapping("/{studyId}/words/review")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<WordReviewResponse> review(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId) {
        WordReviewResponse wordReviews = wordService.getWordReviews(studyId, customOAuth2User.getUserId());
        return CommonResponse.ok(wordReviews);
    }

    @PostMapping("/{studyId}/words/review")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> createReview(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId, @RequestBody CreateWordReviewRequest request) {
        wordService.createReview(studyId, customOAuth2User.getUserId(), request.getCorrectIds(), request.getIncorrectIds());
        return CommonResponse.ok();
    }

    @PutMapping("/{studyId}/words/review")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> updateReview(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId, @RequestBody UpdateWordReviewRequest request) {
        wordService.updateReview(studyId, customOAuth2User.getUserId(), request.getCorrectIds());
        return CommonResponse.ok();
    }
}
