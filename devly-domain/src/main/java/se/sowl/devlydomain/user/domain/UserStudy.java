package se.sowl.devlydomain.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_studies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStudy extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "study_id")
    private Long studyId;

    private boolean isCompleted;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Builder
    public UserStudy(Long userId, Long studyId, LocalDateTime scheduledAt) {
        this.userId = userId;
        this.studyId = studyId;
        this.scheduledAt = scheduledAt;
        this.isCompleted = false;
    }

    public void complete() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }
}
