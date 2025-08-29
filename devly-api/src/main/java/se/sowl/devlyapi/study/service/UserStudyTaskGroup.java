package se.sowl.devlyapi.study.service;

import lombok.Getter;
import se.sowl.devlyapi.study.dto.UserStudyTask;
import se.sowl.devlyapi.study.dto.UserStudyTasksResponse;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeClassification;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
public class UserStudyTaskGroup {
    private final Map<StudyTypeClassification, UserStudyTask> tasks;

    public static UserStudyTaskGroup from(
        List<UserStudy> userStudies,
        Map<Long, StudyType> studyTypeMap,
        Map<StudyTypeClassification, Long> reviewCounts
    ) {
        Map<StudyTypeClassification, UserStudyTask> taskMap = new EnumMap<>(StudyTypeClassification.class);

        for (StudyTypeClassification type : StudyTypeClassification.values()) {
            UserStudy userStudy = findUserStudyByType(userStudies, studyTypeMap, type);
            Long requiredCount = reviewCounts.getOrDefault(type, type.getRequiredCount());
            taskMap.put(type, createTaskForType(userStudy, requiredCount));
        }

        return new UserStudyTaskGroup(taskMap);
    }

    public UserStudyTasksResponse toUserStudyTasksResponse() {
        return new UserStudyTasksResponse(
            tasks.get(StudyTypeClassification.WORD),
            tasks.get(StudyTypeClassification.KNOWLEDGE),
            tasks.get(StudyTypeClassification.PULL_REQUEST),
            tasks.get(StudyTypeClassification.DISCUSSION)
        );
    }

    private UserStudyTaskGroup(Map<StudyTypeClassification, UserStudyTask> tasks) {
        this.tasks = tasks;
    }

    private static UserStudy findUserStudyByType(List<UserStudy> userStudies, Map<Long, StudyType> studyTypeMap, StudyTypeClassification type) {
        return userStudies.stream()
            .filter(us -> type == StudyTypeClassification.fromValue(studyTypeMap.get(us.getStudy().getStudyType().getId()).getName()))
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
