package se.sowl.devlydomain.word.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.sowl.devlydomain.common.BaseTimeEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "word_groups")
@Getter
public class WordGroup extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer sequence;
    private String title;
    private String description;
    private boolean active = true;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Word> words = new ArrayList<>();
}
