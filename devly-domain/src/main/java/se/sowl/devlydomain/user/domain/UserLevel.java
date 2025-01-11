package se.sowl.devlydomain.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.level.domain.Level;

@Entity
@Table(name = "user_levels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private Level level;

    @Builder
    public UserLevel(User user, Level level) {
        this.user = user;
        this.level = level;
    }
}
