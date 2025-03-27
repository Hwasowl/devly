package se.sowl.devlyapi.study.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sowl.devlyapi.common.CommonResponse;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlyapi.study.service.UserStudyService;
import se.sowl.devlydomain.user.domain.CustomOAuth2User;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyController {
    private final UserStudyService userStudyService;

    @GetMapping("/tasks")
    @PreAuthorize("isAuthenticated()")
    public CommonResponse<UserStudyTasksResponse> getUserStudyTasks(@AuthenticationPrincipal CustomOAuth2User user) {
        UserStudyTasksResponse response = userStudyService.getUserStudyTasks(user.getUserId());
        return CommonResponse.ok(response);
    }
}
