package se.sowl.devlyapi.study.dto;

import lombok.Getter;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.domain.StudyTypeEnum;
import se.sowl.devlydomain.user.domain.UserStudy;

import java.util.List;
import java.util.Map;

@Getter
public class UserStudyTaskGroup {
    private final Map<StudyTypeEnum, UserStudyTask> tasks;

    public static UserStudyTaskGroup from(List<UserStudy> userStudies, Map<Long, StudyType> studyTypeMap) {
        return new UserStudyTaskGroup(
            createTask(userStudies, studyTypeMap, StudyTypeEnum.WORD),
            createCompletedTask(),
            createCompletedTask(),
            createCompletedTask()
        );
    }

    private static UserStudyTask createTask(
        List<UserStudy> userStudies,
        Map<Long, StudyType> studyTypeMap,
        StudyTypeEnum type) {
        return userStudies.stream()
            .filter(us -> studyTypeMap.get(us.getStudy().getTypeId()).getName().equals(type.getValue()))
            .findFirst()
            .map(us -> new UserStudyTask(us.getStudy().getId(), type.getRequiredCount(), us.isCompleted()))
            .orElse(new UserStudyTask(null, 0L, false));
    }

    // TODO: 임시로 완료 상태의 task를 반환하는 메서드. 추후 학습 도메인 설계가 된다면 제거되야만 한다.
    private static UserStudyTask createCompletedTask() {
        return new UserStudyTask(null, 0L, true);
    }

    private UserStudyTaskGroup(UserStudyTask word, UserStudyTask knowledge, UserStudyTask pr, UserStudyTask discussion) {
        if (word == null || knowledge == null || pr == null || discussion == null) {
            throw new IllegalArgumentException("All tasks must be provided");
        }

        tasks = Map.of(
            StudyTypeEnum.WORD, word,
            StudyTypeEnum.KNOWLEDGE, knowledge,
            StudyTypeEnum.PULL_REQUEST, pr,
            StudyTypeEnum.DISCUSSION, discussion
        );
    }

    public UserStudyTask getWordTask() {
        return tasks.get(StudyTypeEnum.WORD);
    }

    public UserStudyTask getKnowledgeTask() {
        return tasks.get(StudyTypeEnum.KNOWLEDGE);
    }

    public UserStudyTask getPrTask() {
        return tasks.get(StudyTypeEnum.PULL_REQUEST);
    }

    public UserStudyTask getDiscussionTask() {
        return tasks.get(StudyTypeEnum.DISCUSSION);
    }

    public UserStudyTasksResponse toUserStudyTasksResponse() {
        return new UserStudyTasksResponse(
            getWordTask(),
            getKnowledgeTask(),
            getPrTask(),
            getDiscussionTask()
        );
    }
}
