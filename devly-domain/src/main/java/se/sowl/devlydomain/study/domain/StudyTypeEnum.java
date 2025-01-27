package se.sowl.devlydomain.study.domain;

import lombok.Getter;

@Getter
public enum StudyTypeEnum {
    WORD("word"),
    KNOWLEDGE("knowledge"),
    PULL_REQUEST("pr"),
    DISCUSSION("discussion");

    private final String value;

    StudyTypeEnum(String value) {
        this.value = value;
    }
}
