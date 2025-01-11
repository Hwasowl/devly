package se.sowl.devlydomain.study.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "study_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "base_exp")
    private Integer baseExp;

    @Builder
    public StudyType(String name, Integer baseExp) {
        this.name = name;
        this.baseExp = baseExp;
    }
}
