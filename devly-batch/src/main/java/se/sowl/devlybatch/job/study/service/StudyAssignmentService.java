package se.sowl.devlybatch.job.study.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlybatch.job.study.cache.StudyCache;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyStatus;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyAssignmentService {
    private final UserStudyRepository userStudyRepository;
    private final StudyRepository studyRepository;
    private final StudyCache studyCache;

    @Transactional
    public List<UserStudy> assignNextStudiesForPage(List<UserStudy> completedUserStudies) {
        if (completedUserStudies.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = extractUserIds(completedUserStudies);
        Map<Long, List<UserStudy>> userToStudiesMap = fetchAllUserStudies(userIds);
        ensureCachePopulated();

        return processCompletedStudies(completedUserStudies, userToStudiesMap);
    }

    private Set<Long> extractUserIds(List<UserStudy> studies) {
        return studies.stream()
            .map(userStudy -> userStudy.getUser().getId())
            .collect(Collectors.toSet());
    }

    private Map<Long, List<UserStudy>> fetchAllUserStudies(Set<Long> userIds) {
        Map<Long, List<UserStudy>> result = new HashMap<>();
        for (Long userId : userIds) {
            List<UserStudy> userStudies = userStudyRepository.findAllWithStudyByUserId(userId);
            result.put(userId, userStudies);
        }
        return result;
    }

    private void ensureCachePopulated() {
        if (studyCache.isEmpty()) {
            List<Study> allStudies = studyRepository.findAllByStatusOrderById(StudyStatus.CONNECTED);
            studyCache.cacheStudies(allStudies);
        }
    }

    private List<UserStudy> processCompletedStudies(
        List<UserStudy> completedStudies,
        Map<Long, List<UserStudy>> userToStudiesMap
    ) {
        List<UserStudy> newAssignments = new ArrayList<>();
        for (UserStudy completedStudy : completedStudies) {
            Long userId = completedStudy.getUser().getId();
            Study study = completedStudy.getStudy();
            if (canAssignNextStudy(completedStudy.getStudy(), userToStudiesMap.get(userId))) {
                Study nextStudy = findNextStudy(study);
                if (nextStudy != null) {
                    UserStudy newUserStudy = createNextUserStudy(completedStudy, nextStudy);
                    newAssignments.add(newUserStudy);
                    userToStudiesMap.get(userId).add(newUserStudy);
                }
            }
        }
        return newAssignments;
    }

    private boolean canAssignNextStudy(Study study, List<UserStudy> userStudies) {
        if (userStudies == null) {
            return true;
        }
        return userStudies.stream()
            .filter(us -> !us.isCompleted())
            .noneMatch(us -> isSameStudyType(us.getStudy(), study) ||
                isNextStudy(study, us.getStudy()));
    }

    private boolean isNextStudy(Study currentStudy, Study potentialNextStudy) {
        List<Study> typeStudies = studyCache.getStudies(
            currentStudy.getStudyType().getId(),
            currentStudy.getDeveloperType().getId()
        );

        int currentIndex = findStudyIndex(typeStudies, currentStudy.getId());
        if (currentIndex == -1 || currentIndex == typeStudies.size() - 1) {
            return false;
        }

        Study nextStudy = typeStudies.get(currentIndex + 1);
        return nextStudy.getId().equals(potentialNextStudy.getId());
    }

    private boolean isSameStudyType(Study study1, Study study2) {
        return Objects.equals(study1.getStudyType().getId(), study2.getStudyType().getId()) &&
            Objects.equals(study1.getDeveloperType().getId(), study2.getDeveloperType().getId());
    }

    private Study findNextStudy(Study currentStudy) {
        List<Study> studies = studyCache.getStudies(
            currentStudy.getStudyType().getId(),
            currentStudy.getDeveloperType().getId()
        );

        return findNextInList(studies, currentStudy);
    }

    private Study findNextInList(List<Study> studies, Study currentStudy) {
        if (studies.isEmpty()) {
            return null;
        }

        int currentIndex = findStudyIndex(studies, currentStudy.getId());
        if (currentIndex == -1 || currentIndex == studies.size() - 1) {
            return null;
        }

        return studies.get(currentIndex + 1);
    }

    private int findStudyIndex(List<Study> studies, Long id) {
        for (int i = 0; i < studies.size(); i++) {
            if (studies.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private UserStudy createNextUserStudy(UserStudy completed, Study nextStudy) {
        return UserStudy.builder()
            .user(completed.getUser())
            .study(nextStudy)
            .scheduledAt(LocalDateTime.now())
            .build();
    }
}
