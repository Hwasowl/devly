package se.sowl.devlyapi.study.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.word.repository.WordReviewRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StudyReviewService {
    private final WordReviewRepository wordReviewRepository;

    public Map<StudyTypeEnum, Long> calculateReviewCounts(List<UserStudy> userStudies, Map<Long, StudyType> studyTypeMap) {
        Map<StudyTypeEnum, Long> counts = new EnumMap<>(StudyTypeEnum.class);
        for (UserStudy userStudy : userStudies) {
            StudyTypeEnum type = StudyTypeEnum.fromValue(studyTypeMap.get(userStudy.getStudy().getStudyType().getId()).getName());
            if (type == StudyTypeEnum.WORD && !userStudy.isCompleted()) {
                boolean hasReviews = wordReviewRepository.existsByUserStudyId(userStudy.getId());
                counts.put(type, hasReviews
                    ? wordReviewRepository.countByUserStudyIdAndCorrectIsFalse(userStudy.getId())
                    : type.getRequiredCount()
                );
            }
        }
        return counts;
    }
}
