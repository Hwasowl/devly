package se.sowl.devlydomain.user.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.BatchSize;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.level.domain.Level;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_type_id")
    private DeveloperType developerType;

    @Column(nullable = false)
    private String name;

    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<UserStudy> userStudies = new ArrayList<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserLevel> userLevels = new ArrayList<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserExpLog> userExpLogs = new ArrayList<>();

    private Integer currentExp = 0;

    @Builder
    public User(Long id, DeveloperType developerType, String name, String nickname, String email, String provider, Integer currentExp) {
        this.id = id;
        this.developerType = developerType;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
        this.currentExp = currentExp != null ? currentExp : 0;
    }

    public void addExp(Integer exp, String reason) {
        this.currentExp += exp;
        this.userExpLogs.add(UserExpLog.builder()
                .user(this)
                .exp(exp)
                .reason(reason)
                .build());
    }

    public boolean canPromoteToLevel(Level level) {
        return level.canPromote(this.currentExp);
    }

    public void promoteToLevel(Level level) {
        if (!canPromoteToLevel(level)) {
            throw new IllegalStateException("Not enough experience to promote to level: " + level.getLevel());
        }
        this.userLevels.add(UserLevel.builder()
                .user(this)
                .level(level)
                .build());
    }

    public Level getCurrentLevel() {
        return userLevels.stream()
                .map(UserLevel::getLevel)
                .max((l1, l2) -> Integer.compare(l1.getLevel(), l2.getLevel()))
                .orElse(null);
    }

    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               provider != null && !provider.trim().isEmpty();
    }

    public void validateForRegistration() {
        if (!isValid()) {
            throw new IllegalArgumentException("User information is incomplete for registration");
        }
        if (developerType == null) {
            throw new IllegalArgumentException("Developer type must be specified");
        }
    }
}
