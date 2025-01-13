package se.sowl.devlyapi.study.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.devlyapi.common.CommonResponse;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    @GetMapping("/tasks")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<Void> getUserStudyTasks(@AuthenticationPrincipal OAuth2User user) {
        return CommonResponse.ok();
    }
}
