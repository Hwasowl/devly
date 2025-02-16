package se.sowl.devlyapi.study.service;

import lombok.Getter;
import se.sowl.devlyapi.study.dto.UserStudyTask;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class UserStudyTaskGroup {
    private final Map<StudyTypeEnum, UserStudyTask> tasks;

    public static UserStudyTaskGroup from(
        List<UserStudy> userStudies,
        Map<Long, StudyType> studyTypeMap,
        Map<StudyTypeEnum, Long> reviewCounts
    ) {
        return new UserStudyTaskGroup(userStudies, studyTypeMap, reviewCounts);
    }

    private UserStudyTaskGroup(
        List<UserStudy> userStudies,
        Map<Long, StudyType> studyTypeMap,
        Map<StudyTypeEnum, Long> reviewCounts
    ) {
        this.tasks = Arrays.stream(StudyTypeEnum.values())
            .collect(Collectors.toMap(
                type -> type,
                type -> createTaskForType(
                    findUserStudyByType(userStudies, studyTypeMap, type),
                    reviewCounts.getOrDefault(type, type.getRequiredCount())
                ),
                (existing, replacement) -> existing, () -> new EnumMap<>(StudyTypeEnum.class)
            ));
    }

    private UserStudy findUserStudyByType(List<UserStudy> userStudies, Map<Long, StudyType> studyTypeMap, StudyTypeEnum type) {
        return userStudies.stream()
            .filter(us -> type == StudyTypeEnum.fromValue(studyTypeMap.get(us.getStudy().getTypeId()).getName()))
            .findFirst()
            .orElse(null);
    }

    private UserStudyTask createTaskForType(UserStudy userStudy, Long requiredCount) {
        if (userStudy == null) {
            return new UserStudyTask(null, 0L, false);
        }
        if (userStudy.isCompleted()) {
            return new UserStudyTask(userStudy.getStudy().getId(), 0L, true);
        }
        return new UserStudyTask(userStudy.getStudy().getId(), requiredCount, false);
    }

    public UserStudyTasksResponse toUserStudyTasksResponse() {
        return new UserStudyTasksResponse(
            tasks.get(StudyTypeEnum.WORD),
            tasks.get(StudyTypeEnum.KNOWLEDGE),
            tasks.get(StudyTypeEnum.PULL_REQUEST),
            tasks.get(StudyTypeEnum.DISCUSSION)
        );
    }
}
