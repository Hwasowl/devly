package se.sowl.devlyapi.pr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.pr.dto.PrChangedFilesResponse;
import se.sowl.devlyapi.pr.dto.PrResponse;
import se.sowl.devlyapi.study.service.UserStudyService;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrChangedFile;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.pr.repository.PrChangedFileRepository;
import se.sowl.devlydomain.pr.repository.PrLabelRepository;
import se.sowl.devlydomain.pr.repository.PrRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrService {
    private final UserStudyService userStudyService;
    private final PrRepository prRepository;
    private final PrLabelRepository prLabelRepository;
    private final PrChangedFileRepository prChangedFileRepository;

    public PrResponse getPr(Long userId, Long studyId) {
        userStudyService.isUserStudyExist(userId, studyId);
        Pr pr = prRepository.findByStudyId(studyId);
        List<PrLabel> prLabels = prLabelRepository.findAllByPrId(pr.getId());
        return PrResponse.from(pr, prLabels);
    }

    public PrChangedFilesResponse getChangedFiles(Long prId) {
        List<PrChangedFile> files = prChangedFileRepository.findByPrId(prId);
        return PrChangedFilesResponse.from(files);
    }
}
