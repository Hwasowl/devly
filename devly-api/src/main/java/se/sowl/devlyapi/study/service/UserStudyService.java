package se.sowl.devlyapi.study.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlyapi.word.exception.NotAssignmentWordStudyException;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStudyService {
    private final UserStudyRepository userStudyRepository;
    private final StudyTypeRepository studyTypeRepository;
    private final StudyReviewService studyReviewService;

    public void isUserStudyExist(Long userId, Long studyId) {
        Optional<UserStudy> optionalUserStudy = userStudyRepository.findByUserIdAndStudyId(userId, studyId);
        if (optionalUserStudy.isEmpty()) {
            throw new NotAssignmentWordStudyException();
        }
    }

    public UserStudy getUserStudy(Long userId, Long studyId) {
        return userStudyRepository.findByUserIdAndStudyId(userId, studyId)
            .orElseThrow(NotAssignmentWordStudyException::new);
    }

    public UserStudyTasksResponse getUserStudyTasks(Long userId) {
        List<UserStudy> userStudies = userStudyRepository.findLatestByUserIdWithStudyType(userId);
        Map<Long, StudyType> studyTypeMap = studyTypeRepository.findAll()
            .stream()
            .collect(Collectors.toMap(StudyType::getId, Function.identity()));
        Map<StudyTypeEnum, Long> reviewCounts = studyReviewService.calculateReviewCounts(userStudies, studyTypeMap);
        UserStudyTaskGroup taskGroup = UserStudyTaskGroup.from(userStudies, studyTypeMap, reviewCounts);
        return taskGroup.toUserStudyTasksResponse();
    }
}
