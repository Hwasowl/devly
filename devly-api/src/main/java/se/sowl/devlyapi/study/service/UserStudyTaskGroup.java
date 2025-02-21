package se.sowl.devlyapi.study.service;

import lombok.Getter;
import se.sowl.devlyapi.study.dto.UserStudyTask;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
public class UserStudyTaskGroup {
    private final Map<StudyTypeEnum, UserStudyTask> tasks;

    public static UserStudyTaskGroup from(
        List<UserStudy> userStudies,
        Map<Long, StudyType> studyTypeMap,
        Map<StudyTypeEnum, Long> reviewCounts
    ) {
        Map<StudyTypeEnum, UserStudyTask> taskMap = new EnumMap<>(StudyTypeEnum.class);

        for (StudyTypeEnum type : StudyTypeEnum.values()) {
            UserStudy userStudy = findUserStudyByType(userStudies, studyTypeMap, type);
            Long requiredCount = reviewCounts.getOrDefault(type, type.getRequiredCount());
            taskMap.put(type, createTaskForType(userStudy, requiredCount));
        }

        return new UserStudyTaskGroup(taskMap);
    }

    public UserStudyTasksResponse toUserStudyTasksResponse() {
        return new UserStudyTasksResponse(
            tasks.get(StudyTypeEnum.WORD),
            tasks.get(StudyTypeEnum.KNOWLEDGE),
            tasks.get(StudyTypeEnum.PULL_REQUEST),
            tasks.get(StudyTypeEnum.DISCUSSION)
        );
    }

    private UserStudyTaskGroup(Map<StudyTypeEnum, UserStudyTask> tasks) {
        this.tasks = tasks;
    }

    private static UserStudy findUserStudyByType(List<UserStudy> userStudies, Map<Long, StudyType> studyTypeMap, StudyTypeEnum type) {
        return userStudies.stream()
            .filter(us -> type == StudyTypeEnum.fromValue(studyTypeMap.get(us.getStudy().getTypeId()).getName()))
            .findFirst()
            .orElse(null);
    }

    private static UserStudyTask createTaskForType(UserStudy userStudy, Long requiredCount) {
        if (userStudy == null) {
            return new UserStudyTask(null, 0L, false);
        }
        if (userStudy.isCompleted()) {
            return new UserStudyTask(userStudy.getStudy().getId(), 0L, true);
        }
        return new UserStudyTask(userStudy.getStudy().getId(), requiredCount, false);
    }
}
