package se.sowl.devlydomain.study.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.developer.domain.DeveloperType;

@Entity
@Table(name = "studies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private StudyType studyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_type_id")
    private DeveloperType developerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudyStatus status;

    public void connect() {
        if (this.status == StudyStatus.CONNECTED) {
            throw new IllegalStateException("Study is already connected");
        }
        this.status = StudyStatus.CONNECTED;
    }

    public void disconnect() {
        if (this.status == StudyStatus.UNCONNECTED) {
            throw new IllegalStateException("Study is already disconnected");
        }
        this.status = StudyStatus.UNCONNECTED;
    }

    public boolean isConnected() {
        return this.status == StudyStatus.CONNECTED;
    }

    public boolean isValid() {
        return studyType != null && developerType != null && status != null;
    }

    @Builder
    public Study(StudyType studyType, DeveloperType developerType) {
        this.studyType = studyType;
        this.developerType = developerType;
        this.status = StudyStatus.UNCONNECTED;
    }
}
