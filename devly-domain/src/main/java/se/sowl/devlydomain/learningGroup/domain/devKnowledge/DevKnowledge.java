package se.sowl.devlydomain.learningGroup.domain.devKnowledge;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;
import se.sowl.devlydomain.learningGroup.domain.LearningGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "dev_knowledge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DevKnowledge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private LearningGroup group;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 200)
    private String subtitle;

    @OneToMany(mappedBy = "devKnowledge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentSection> sections = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
        name = "dev_knowledge_tags",
        joinColumns = @JoinColumn(name = "dev_knowledge_id")
    )
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Builder
    private DevKnowledge(
        LearningGroup group,
        String title,
        String subtitle
    ) {
        this.group = group;
        this.title = title;
        this.subtitle = subtitle;
    }

    public void addSection(ContentSection section) {
        this.sections.add(section);
        section.setDevKnowledge(this);
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }
}
