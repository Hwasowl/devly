package se.sowl.devlyapi.study.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeClassification;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.word.repository.WordReviewRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StudyReviewService {
    private final WordReviewRepository wordReviewRepository;

    public Map<StudyTypeClassification, Long> calculateReviewCounts(List<UserStudy> userStudies, Map<Long, StudyType> studyTypeMap) {
        Map<StudyTypeClassification, Long> counts = new EnumMap<>(StudyTypeClassification.class);
        for (UserStudy userStudy : userStudies) {
            StudyTypeClassification type = StudyTypeClassification.fromValue(studyTypeMap.get(userStudy.getStudy().getStudyType().getId()).getName());
            if (type == StudyTypeClassification.WORD && !userStudy.isCompleted()) {
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
