package se.sowl.devlydomain.study.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum StudyTypeEnum {
    WORD("word", 5L),
    KNOWLEDGE("knowledge", 3L),
    PULL_REQUEST("pr", 1L),
    DISCUSSION("discussion", 1L);

    private final String value;
    private final Long requiredCount;

    StudyTypeEnum(String value, Long requiredCount) {
        this.value = value;
        this.requiredCount = requiredCount;
    }

    public static StudyTypeEnum fromValue(String value) {
        return Arrays.stream(values())
            .filter(type -> type.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown study type: " + value));
    }
}
