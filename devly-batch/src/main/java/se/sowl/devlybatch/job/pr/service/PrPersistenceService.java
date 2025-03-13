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
        Long prId = savedPr.getId();
        saveChangedFiles(prWithRelations, prId);
        saveLabels(prWithRelations, prId);
        saveComments(prWithRelations, prId);
    }

    private void saveChangedFiles(PrWithRelations prWithRelations, Long prId) {
        if (!prWithRelations.getChangedFiles().isEmpty()) {
            List<PrChangedFile> changedFilesWithPrId = prWithRelations.getChangedFiles().stream()
                .map(file -> createPrChangedFileWithPrId(file, prId))
                .collect(Collectors.toList());
            prChangedFileRepository.saveAll(changedFilesWithPrId);
        }
    }

    private void saveLabels(PrWithRelations prWithRelations, Long prId) {
        if (!prWithRelations.getLabels().isEmpty()) {
            List<PrLabel> labelsWithPrId = prWithRelations.getLabels().stream()
                .map(label -> createPrLabelWithPrId(label, prId))
                .collect(Collectors.toList());
            prLabelRepository.saveAll(labelsWithPrId);
        }
    }


    private void saveComments(PrWithRelations prWithRelations, Long prId) {
        if (!prWithRelations.getComments().isEmpty()) {
            List<PrComment> commentsWithPrId = prWithRelations.getComments().stream()
                .map(comment -> createPrCommentWithPrId(comment, prId))
                .collect(Collectors.toList());
            prCommentRepository.saveAll(commentsWithPrId);
        }
    }

    private PrChangedFile createPrChangedFileWithPrId(PrChangedFile original, Long prId) {
        return PrChangedFile.builder()
            .prId(prId)
            .fileName(original.getFileName())
            .language(original.getLanguage())
            .content(original.getContent())
            .build();
    }

    private PrLabel createPrLabelWithPrId(PrLabel original, Long prId) {
        return new PrLabel(prId, original.getLabel());
    }

    private PrComment createPrCommentWithPrId(PrComment original, Long prId) {
        return new PrComment(prId, original.getIdx(), original.getContent());
    }
}
