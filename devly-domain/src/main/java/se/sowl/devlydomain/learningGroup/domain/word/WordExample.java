package se.sowl.devlydomain.learningGroup.domain.word;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordExample {

    @Column(length = 200)
    private String source;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(length = 100)
    private String highlight;

    @Column(columnDefinition = "TEXT")
    private String context;

    @Builder
    private WordExample(
        String source,
        String text,
        String highlight,
        String context
    ) {
        this.source = source;
        this.text = text;
        this.highlight = highlight;
        this.context = context;
    }
}
