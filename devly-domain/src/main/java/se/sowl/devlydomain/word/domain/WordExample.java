package se.sowl.devlydomain.word.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class WordExample {
    private String source;
    private String text;
    private String highlight;
    private String context;
}
