package se.sowl.devlydomain.study.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "study_types")
@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class StudyType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "base_exp")
    private Long baseExp;

    @Builder
    public StudyType(String name, Long baseExp) {
        this.name = name;
        this.baseExp = baseExp;
    }

    public StudyType findStudyType(StudyTypeEnum typeEnum, List<StudyType> studyTypes) {
        return studyTypes.stream()
            .filter(st -> typeEnum == StudyTypeEnum.fromValue(st.getName()))
            .findFirst()
            .orElse(null);
    }
}
