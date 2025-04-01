package se.sowl.devlyapi.pr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.pr.dto.PrResponse;
import se.sowl.devlyapi.pr.exception.IncompletePrReviewException;
import se.sowl.devlyapi.study.service.UserStudyService;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrComment;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.pr.repository.PrLabelRepository;
import se.sowl.devlydomain.pr.repository.PrRepository;
import se.sowl.devlydomain.pr.repository.PrReviewRepository;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrService {
    private final UserStudyService userStudyService;
    private final PrRepository prRepository;
    private final PrLabelRepository prLabelRepository;
    private final PrReviewRepository prReviewRepository;
    private final PrCommentService prCommentService;

    @Transactional
    public void complete(Long userId, Long prId, Long studyId) {
        validateCanComplete(prId);
        UserStudy userStudy = userStudyService.getUserStudy(userId, studyId);
        userStudy.complete();
    }

    public PrResponse getPrResponse(Long userId, Long studyId) {
        userStudyService.isUserStudyExist(userId, studyId);
        Pr pr = prRepository.findByStudyId(studyId);
        List<PrLabel> prLabels = prLabelRepository.findAllByPrId(pr.getId());
        return PrResponse.from(pr, prLabels);
    }

    private void validateCanComplete(Long prId) {
        List<PrComment> comments = prCommentService.getCommentsByPrId(prId);
        if (comments.isEmpty()) return;
        for (PrComment comment : comments) {
            if (prReviewRepository.findByPrCommentId(comment.getId()).isEmpty()) {
                throw new IncompletePrReviewException("Pr Review Is Not Completed.");
            }
        }
    }
}
