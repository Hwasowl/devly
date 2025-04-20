package se.sowl.devlydomain.pr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.study.domain.Study;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pr")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pr extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    private String description;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pr", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrChangedFile> changedFiles = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pr", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrComment> comments = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pr", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrLabel> labels = new ArrayList<>();

    @Builder
    public Pr(String title, String description, Study study) {
        this.title = title;
        this.description = description;
        this.study = study;
    }
}
