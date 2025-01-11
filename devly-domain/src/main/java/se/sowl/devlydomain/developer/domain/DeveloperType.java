package se.sowl.devlydomain.developer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "developer_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeveloperType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
}
