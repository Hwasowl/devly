package se.sowl.devlyapi.word.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.devlyapi.common.CommonResponse;
import se.sowl.devlyapi.word.dto.WordListOfStudyResponse;
import se.sowl.devlyapi.word.service.WordService;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping("/{studyId}")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<WordListOfStudyResponse> getWords(@AuthenticationPrincipal CustomOAuth2User customOAuth2User, @PathVariable Long studyId) {
        WordListOfStudyResponse response = wordService.getList(customOAuth2User.getUserId(), studyId);
        return CommonResponse.ok(response);
    }
}
