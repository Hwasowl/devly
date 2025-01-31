package se.sowl.devlyapi.study.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.study.dto.UserStudyTaskGroup;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {
    private final UserStudyRepository userStudyRepository;
    private final StudyTypeRepository studyTypeRepository;

    public UserStudyTasksResponse getUserStudyTasks(Long userId) {
        List<UserStudy> userStudies = userStudyRepository.findLatestByUserIdWithStudyType(userId);
        Map<Long, StudyType> studyTypeMap = studyTypeRepository.findAll()
            .stream()
            .collect(Collectors.toMap(StudyType::getId, Function.identity()));
        UserStudyTaskGroup taskGroup = UserStudyTaskGroup.from(userStudies, studyTypeMap);
        return taskGroup.toUserStudyTasksResponse();
    }
}

