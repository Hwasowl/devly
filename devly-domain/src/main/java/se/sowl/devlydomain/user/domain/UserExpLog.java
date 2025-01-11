package se.sowl.devlydomain.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "user_exp_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserExpLog extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Integer exp;

    private String reason;

    @Builder
    public UserExpLog(User user, Integer exp, String reason) {
        this.user = user;
        this.exp = exp;
        this.reason = reason;
    }
}
