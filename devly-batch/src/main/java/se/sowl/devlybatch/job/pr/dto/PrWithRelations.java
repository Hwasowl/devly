package se.sowl.devlybatch.job.pr.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.domain.PrLabel;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PrWithRelations {
    private final Pr pr;
    private final List<PrChangedFile> changedFiles;
    private final List<PrLabel> labels;
    private final List<PrComment> comments;
}
