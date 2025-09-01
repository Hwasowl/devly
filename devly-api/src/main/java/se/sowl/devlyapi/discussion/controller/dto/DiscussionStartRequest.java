package se.sowl.devlyapi.discussion.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionStartRequest {
    @NotNull(message = "User ID는 필수입니다")
    private Long userId;
    
    @NotNull(message = "Topic ID는 필수입니다")
    private Long topicId;
}