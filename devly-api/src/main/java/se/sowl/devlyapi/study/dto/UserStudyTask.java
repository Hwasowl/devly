package se.sowl.devlyapi.study.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserStudyTask {
    private final Long studyId;
    private final Long total;
    private final boolean completed;
}
