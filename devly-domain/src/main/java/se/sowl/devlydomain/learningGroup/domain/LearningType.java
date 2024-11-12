package se.sowl.devlydomain.learningGroup.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LearningType {
    WORD("단어 학습"),
    DEV_KNOWLEDGE("개발 지식");

    private final String description;
}
