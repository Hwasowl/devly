package se.sowl.devlydomain.study.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum StudyTypeClassification {
    WORD("word", 5L, 1L),
    KNOWLEDGE("knowledge", 3L, 2L),
    PULL_REQUEST("pr", 1L, 3L),
    DISCUSSION("discussion", 1L, 4L);

    private final String value;
    private final Long requiredCount;
    private final Long id;

    StudyTypeClassification(String value, Long requiredCount, Long id) {
        this.value = value;
        this.requiredCount = requiredCount;
        this.id = id;
    }

    public static StudyTypeClassification fromValue(String value) {
        return Arrays.stream(values())
            .filter(type -> type.getValue().equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown study type: " + value));
    }

    public boolean isValid() {
        return value != null && !value.trim().isEmpty() && requiredCount > 0;
    }
}