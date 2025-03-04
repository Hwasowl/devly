package se.sowl.devlyapi.pr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.pr.dto.PrResponse;
import se.sowl.devlyapi.study.service.UserStudyService;
import se.sowl.devlydomain.pr.domain.Pr;
import se.sowl.devlydomain.pr.domain.PrLabel;
import se.sowl.devlydomain.pr.repository.PrLabelRepository;
import se.sowl.devlydomain.pr.repository.PrRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrService {
    private final UserStudyService userStudyService;
    private final PrRepository prRepository;
    private final PrLabelRepository prLabelRepository;

    public PrResponse getPr(Long userId, Long studyId) {
        userStudyService.isUserStudyExist(userId, studyId);
        Pr pr = prRepository.findByStudyId(studyId);
        List<PrLabel> prLabels = prLabelRepository.findAllByPrId(pr.getId());
        return PrResponse.from(pr, prLabels);
    }

    // file changed 조회

    // pr 커멘트 응답 (분리?)
}
