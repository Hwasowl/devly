package se.sowl.devlydomain.pr.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

@Entity
@Table(name = "pr_changed_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrChangedFile extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pull_request_id")
    private Long pullRequestId;

    @Column(name = "file_name")
    private String fileName;

    private String language;

    private String content;
}
