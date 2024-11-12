package se.sowl.devlydomain.learningGroup.domain.devKnowledge;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "content_sections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentSection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dev_knowledge_id", nullable = false)
    private DevKnowledge devKnowledge;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SectionType type;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(length = 200)
    private String imageSrc;

    @ElementCollection
    @CollectionTable(
        name = "section_examples",
        joinColumns = @JoinColumn(name = "section_id")
    )
    @Column(name = "example")
    private List<String> examples = new ArrayList<>();

    @Builder
    private ContentSection(
        SectionType type,
        String title,
        String text,
        String imageSrc
    ) {
        this.type = type;
        this.title = title;
        this.text = text;
        this.imageSrc = imageSrc;
    }

    void setDevKnowledge(DevKnowledge devKnowledge) {
        this.devKnowledge = devKnowledge;
    }
}
