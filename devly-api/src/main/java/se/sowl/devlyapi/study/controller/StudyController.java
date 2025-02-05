package se.sowl.devlyapi.study.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.sowl.devlyapi.common.CommonResponse;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlyapi.study.dto.WordReviewResponse;
import se.sowl.devlyapi.study.service.StudyService;
import se.sowl.devlyapi.study.dto.WordReviewRequest;
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
    public CommonResponse<Void> review(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId, @RequestBody WordReviewRequest request) {
        wordService.review(studyId, customOAuth2User.getUserId(), request.getCorrectIds(), request.getIncorrectIds());
        return CommonResponse.ok();
    }
}
