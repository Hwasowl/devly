package se.sowl.devlybatch.job.userStudy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlybatch.job.study.cache.StudyCache;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatusEnum;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyAssignmentService {
    private final UserStudyRepository userStudyRepository;
    private final StudyRepository studyRepository;
    private final StudyCache studyCache;

    @Transactional
    public UserStudy assignNextStudy(UserStudy completedUserStudy) {
        Study completedStudy = getCompletedStudy(completedUserStudy);
        if (completedStudy == null) return null;

        Study nextStudy = findNextStudyFromCache(completedStudy);
        if (nextStudy == null) return null;

        return buildNextUserStudy(completedUserStudy, nextStudy);
    }

    @Transactional(readOnly = true)
    public Page<UserStudy> findCompletedStudiesWithoutNext(int page, int size) {
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        return userStudyRepository.findCompletedStudiesWithoutNext(
            yesterday, todayStart, todayStart,
            PageRequest.of(page, size));
    }

    private Study getCompletedStudy(UserStudy completed) {
        Study completedStudy = completed.getStudy();
        if (completedStudy == null) {
            log.warn("Completed study not found: {}", completed.getId());
            return null;
        }
        return completedStudy;
    }

    private Study findNextStudyFromCache(Study completedStudy) {
        List<Study> cachedStudies = studyCache.getStudies(
            completedStudy.getTypeId(),
            completedStudy.getDeveloperTypeId()
        );

        if (cachedStudies.isEmpty()) {
            log.info("No cached studies found, falling back to database");
            List<Study> orderedStudies = studyRepository.findAllByStatusOrderById(StudyStatusEnum.CONNECTED);
            studyCache.cacheStudies(orderedStudies);
            cachedStudies = studyCache.getStudies(
                completedStudy.getTypeId(),
                completedStudy.getDeveloperTypeId()
            );
        }

        return findNextStudyInList(cachedStudies, completedStudy);
    }

    private Study findNextStudyInList(List<Study> studies, Study currentStudy) {
        if (studies.isEmpty()) {
            return null;
        }

        int currentIndex = studies.indexOf(currentStudy);
        if (currentIndex == -1 || currentIndex == studies.size() - 1) {
            return null;
        }

        return studies.get(currentIndex + 1);
    }

    private UserStudy buildNextUserStudy(UserStudy completedUserStudy, Study nextStudy) {
        return UserStudy.builder()
            .userId(completedUserStudy.getUserId())
            .study(nextStudy)
            .scheduledAt(LocalDateTime.now())
            .build();
    }
}
