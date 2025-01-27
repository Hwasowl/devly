package se.sowl.devlydomain.developer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "developer_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeveloperType extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    public DeveloperType(String name) {
        this.name = name;
    }
}
