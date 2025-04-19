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

    @Builder
    public User(Long id, DeveloperType developerType, String name, String nickname, String email, String provider) {
        this.id = id;
        this.developerType = developerType;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
    }
}
