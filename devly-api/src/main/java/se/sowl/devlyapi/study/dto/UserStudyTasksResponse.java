package se.sowl.devlyapi.study.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserStudyTasksResponse {
    private final UserStudyTask word;
    private final UserStudyTask knowledge;
    private final UserStudyTask pr;
    private final UserStudyTask discussion;
}
