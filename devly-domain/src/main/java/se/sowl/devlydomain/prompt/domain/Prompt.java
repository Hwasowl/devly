package se.sowl.devlydomain.prompt.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "prompts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Prompt extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "developer_type_id")
    private Long developerTypeId;

    @Column(name = "study_type_id")
    private Long studyTypeId;

    @Column(length = 50000)
    private String content;

    public Prompt(Long developerTypeId, Long studyTypeId, String content) {
        this.developerTypeId = developerTypeId;
        this.studyTypeId = studyTypeId;
        this.content = content;
    }
}

