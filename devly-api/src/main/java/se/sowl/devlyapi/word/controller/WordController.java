package se.sowl.devlyapi.word.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import se.sowl.devlyapi.common.CommonResponse;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.dto.reviews.CreateWordReviewRequest;
import se.sowl.devlyapi.word.dto.reviews.UpdateWordReviewRequest;
import se.sowl.devlyapi.word.dto.reviews.WordReviewResponse;
import se.sowl.devlyapi.word.service.WordReviewService;
import se.sowl.devlyapi.word.service.WordService;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;
    private final WordReviewService wordReviewService;

    @GetMapping("/{studyId}")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<WordListOfStudyResponse> getWords(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId) {
        WordListOfStudyResponse response = wordService.getList(customOAuth2User.getUserId(), studyId);
        return CommonResponse.ok(response);
    }

    @GetMapping("/review/study/{studyId}")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<WordReviewResponse> review(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId) {
        WordReviewResponse wordReviews = wordService.getWordReviews(studyId, customOAuth2User.getUserId());
        return CommonResponse.ok(wordReviews);
    }

    @PostMapping("/review/study/{studyId}")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> createReview(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId, @RequestBody CreateWordReviewRequest request) {
        wordReviewService.createReview(studyId, customOAuth2User.getUserId(), request.getCorrectIds(), request.getIncorrectIds());
        return CommonResponse.ok();
    }

    @PutMapping("/review/study/{studyId}")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> updateReview(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId, @RequestBody UpdateWordReviewRequest request) {
        wordReviewService.updateReview(studyId, customOAuth2User.getUserId(), request.getCorrectIds());
        return CommonResponse.ok();
    }
}
