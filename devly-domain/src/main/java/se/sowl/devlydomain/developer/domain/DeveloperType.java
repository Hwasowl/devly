package se.sowl.devlydomain.developer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Builder
    public DeveloperType(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
