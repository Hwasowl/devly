package se.sowl.devlybatch.job.pr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlybatch.job.pr.dto.PrWithRelations;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.pr.repository.PrChangedFileRepository;
import se.sowl.devlydomain.pr.repository.PrCommentRepository;
import se.sowl.devlydomain.pr.repository.PrLabelRepository;
import se.sowl.devlydomain.pr.repository.PrRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrPersistenceService {
    private final PrRepository prRepository;
    private final PrChangedFileRepository prChangedFileRepository;
    private final PrLabelRepository prLabelRepository;
    private final PrCommentRepository prCommentRepository;

    @Transactional
    public void savePrWithRelations(PrWithRelations prWithRelations) {
        Pr savedPr = prRepository.save(prWithRelations.getPr());
        saveChangedFiles(prWithRelations, savedPr);
        saveLabels(prWithRelations, savedPr);
        saveComments(prWithRelations, savedPr);
    }

    private void saveChangedFiles(PrWithRelations prWithRelations, Pr pr) {
        if (!prWithRelations.getChangedFiles().isEmpty()) {
            List<PrChangedFile> changedFilesWithPrId = prWithRelations.getChangedFiles().stream()
                .map(file -> createPrChangedFileWithPrId(file, pr))
                .collect(Collectors.toList());
            prChangedFileRepository.saveAll(changedFilesWithPrId);
        }
    }

    private void saveLabels(PrWithRelations prWithRelations, Pr pr) {
        if (!prWithRelations.getLabels().isEmpty()) {
            List<PrLabel> labelsWithPrId = prWithRelations.getLabels().stream()
                .map(label -> createPrLabel(label, pr))
                .collect(Collectors.toList());
            prLabelRepository.saveAll(labelsWithPrId);
        }
    }


    private void saveComments(PrWithRelations prWithRelations, Pr pr) {
        if (!prWithRelations.getComments().isEmpty()) {
            List<PrComment> commentsWithPrId = prWithRelations.getComments().stream()
                .map(comment -> createPrComment(comment, pr))
                .collect(Collectors.toList());
            prCommentRepository.saveAll(commentsWithPrId);
        }
    }

    private PrChangedFile createPrChangedFileWithPrId(PrChangedFile original, Pr pr) {
        return PrChangedFile.builder()
            .pr(pr)
            .fileName(original.getFileName())
            .language(original.getLanguage())
            .content(original.getContent())
            .build();
    }

    private PrLabel createPrLabel(PrLabel original, Pr pr) {
        return new PrLabel(pr, original.getLabel());
    }

    private PrComment createPrComment(PrComment original, Pr pr) {
        return new PrComment(pr, original.getSequence(), original.getContent());
    }
}
