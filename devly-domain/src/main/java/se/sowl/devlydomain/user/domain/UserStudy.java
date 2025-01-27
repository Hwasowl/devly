package se.sowl.devlydomain.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.study.domain.Study;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    private boolean isCompleted;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Builder
    public UserStudy(Long userId, Study study, LocalDateTime scheduledAt) {
        this.userId = userId;
        this.study = study;
        this.scheduledAt = scheduledAt;
        this.isCompleted = false;
    }

    public void complete() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }
}
