package se.sowl.devlydomain.discussion.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DiscussionStatus {
    READY("준비 완료"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String description;
}