package se.sowl.devlyapi.study.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sowl.devlyapi.study.dto.UserStudyTask;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserStudyRepository;
import se.sowl.devlydomain.word.repository.WordRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {
    private final UserStudyRepository userStudyRepository;
    private final WordRepository wordRepository;
    private final StudyTypeRepository studyTypeRepository;

    public UserStudyTasksResponse getUserStudyTasks(Long userId) {
        List<UserStudy> userStudies = userStudyRepository.findLatestByUserIdWithStudyType(userId);
        Map<Long, StudyType> studyTypeMap = studyTypeRepository.findAll().stream().collect(Collectors.toMap(StudyType::getId, Function.identity()));

        UserStudyTask wordTask = getUserStudyTask(studyTypeMap, userStudies, StudyTypeEnum.WORD);
        UserStudyTask knowledgeTask = getCompletedTask(userStudies, studyTypeMap, StudyTypeEnum.KNOWLEDGE);
        UserStudyTask prTask = getCompletedTask(userStudies, studyTypeMap, StudyTypeEnum.PULL_REQUEST);
        UserStudyTask discussionTask = getCompletedTask(userStudies, studyTypeMap, StudyTypeEnum.DISCUSSION);

        return new UserStudyTasksResponse(wordTask, knowledgeTask, prTask, discussionTask);
    }

    private UserStudyTask getUserStudyTask(Map<Long, StudyType> studyTypeMap, List<UserStudy> userStudies, StudyTypeEnum studyTypeEnum) {
        return userStudies.stream()
            .filter(us -> studyTypeMap.get(us.getStudy().getTypeId()).getName().equals(studyTypeEnum.getValue()))
            .findFirst()
            .map(us -> new UserStudyTask(us.getStudy().getId(), wordRepository.countByStudyId(us.getStudy().getId()), us.isCompleted()))
            .orElse(new UserStudyTask(null, 0L, false));
    }

    // TODO: 각 학습별 도메인 설계가 끝나면 추출 후 정상 처리 되도록 수정 해야만 함.
    private UserStudyTask getCompletedTask(List<UserStudy> userStudies, Map<Long, StudyType> studyTypeMap, StudyTypeEnum studyTypeEnum) {
        return userStudies.stream()
            .filter(us -> {
                studyTypeMap.get(us.getStudy().getTypeId());
                return false;
            })
            .findFirst()
            .map(us -> new UserStudyTask(us.getStudy().getId(), 1L, false))
            .orElse(new UserStudyTask(null, 0L, true));
    }
}
