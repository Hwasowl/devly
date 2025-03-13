package se.sowl.devlybatch.job.userStudy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyAssignmentService {
    private final UserStudyRepository userStudyRepository;
    private final StudyRepository studyRepository;

    @Transactional
    public UserStudy assignNextStudy(UserStudy completedUserStudy) {
        List<Study> orderedStudies = studyRepository.findAllByOrderById();
        Map<Long, Study> studyMap = orderedStudies.stream().collect(Collectors.toMap(Study::getId, s -> s));

        Study completedStudy = getCompletedStudy(completedUserStudy, studyMap);
        if (completedStudy == null) return null;

        Study nextStudy = getNextStudy(completedUserStudy, orderedStudies, completedStudy);
        if (nextStudy == null) return null;

        return UserStudy.builder()
            .userId(completedUserStudy.getUserId())
            .study(nextStudy)
            .scheduledAt(LocalDateTime.now())
            .build();
    }

    @Transactional(readOnly = true)
    public Page<UserStudy> findCompletedStudiesWithoutNext(int page, int size) {
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        return userStudyRepository.findCompletedStudiesWithoutNext(
            yesterday, todayStart, todayStart,
            PageRequest.of(page, size));
    }

    private Study findNextStudy(List<Study> allStudies, Study currentStudy) {
        boolean foundCurrent = false;
        for (Study study : allStudies) {
            if (foundCurrent && isSameStudyType(currentStudy, study)) return study;
            else if (study.getId().equals(currentStudy.getId())) foundCurrent = true;
        }
        return null;
    }

    private Study getCompletedStudy(UserStudy completed, Map<Long, Study> studyMap) {
        Study completedStudy = studyMap.get(completed.getStudy().getId());
        if (completedStudy == null) {
            log.warn("Completed study not found: {}", completed.getId());
            return null;
        }
        return completedStudy;
    }

    private Study getNextStudy(UserStudy completed, List<Study> orderedStudies, Study completedStudy) {
        Study nextStudy = findNextStudy(orderedStudies, completedStudy);
        if (nextStudy == null) {
            log.info("No next study found for user: {}, type: {}, devType: {}",
                completed.getUserId(), completedStudy.getTypeId(), completedStudy.getDeveloperTypeId());
            return null;
        }
        return nextStudy;
    }

    private boolean isSameStudyType(Study currentStudy, Study study) {
        return study.getTypeId().equals(currentStudy.getTypeId()) &&
            study.getDeveloperTypeId().equals(currentStudy.getDeveloperTypeId());
    }
}
