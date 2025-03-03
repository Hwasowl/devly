package se.sowl.devlydomain.pr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "pr")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pr extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Builder
    public Pr(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
