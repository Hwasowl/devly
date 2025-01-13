package se.sowl.devlydomain.user.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "developer_type_id")
    private Long developerTypeId;

    @Column(nullable = false)
    private String name;

    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String provider;

    @Builder
    public User(Long id, Long developerTypeId, String name, String nickname, String email, String provider) {
        this.id = id;
        this.developerTypeId = developerTypeId;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.provider = provider;
    }
}
