package se.sowl.devlydomain.study.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "studies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "developer_type_id")
    private Long developerTypeId;

    private StudyStatus status;

    @Builder
    public Study(Long typeId, Long developerTypeId) {
        this.typeId = typeId;
        this.developerTypeId = developerTypeId;
        this.status = StudyStatus.UNCONNECTED;
    }
}
