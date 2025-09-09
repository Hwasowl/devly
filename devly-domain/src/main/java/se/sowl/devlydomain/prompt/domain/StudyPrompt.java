package se.sowl.devlydomain.prompt.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "study_prompts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyPrompt extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "developer_type_id")
    private Long developerTypeId;

    @Column(name = "study_type_id")
    private Long studyTypeId;

    @Column(name = "role_content", length = 50000)
    private String roleContent;

    @Column(name = "generate_content", length = 50000)
    private String generateContent;

    public StudyPrompt(Long developerTypeId, Long studyTypeId, String roleContent, String generateContent) {
        this.developerTypeId = developerTypeId;
        this.studyTypeId = studyTypeId;
        this.roleContent = roleContent;
        this.generateContent = generateContent;
    }
}