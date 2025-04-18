package se.sowl.devlybatch.job.study;

import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;
import se.sowl.devlydomain.user.domain.User;
import se.sowl.devlydomain.user.domain.UserStudy;
import se.sowl.devlydomain.user.repository.UserRepository;
import se.sowl.devlydomain.user.repository.UserStudyRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class TestDataFactory {
    private final UserRepository userRepository;
    private final StudyRepository studyRepository;
    private final UserStudyRepository userStudyRepository;
    private final StudyTypeRepository studyTypeRepository;
    private final DeveloperTypeRepository developerTypeRepository;

    private final AtomicLong emailCounter = new AtomicLong(0);

    public TestDataFactory(UserRepository userRepository, StudyRepository studyRepository, UserStudyRepository userStudyRepository, StudyTypeRepository studyTypeRepository, DeveloperTypeRepository developerTypeRepository) {
        this.userRepository = userRepository;
        this.studyRepository = studyRepository;
        this.userStudyRepository = userStudyRepository;
        this.studyTypeRepository = studyTypeRepository;
        this.developerTypeRepository = developerTypeRepository;
    }

    public DeveloperType createDeveloperType(String name) {
        return developerTypeRepository.save(new DeveloperType(name));
    }

    public StudyType createStudyType(String name, Long baseExp) {
        return studyTypeRepository.save(new StudyType(name, baseExp));
    }

    public User createUniqueUser(DeveloperType developerType, String nameBase) {
        long uniqueId = emailCounter.incrementAndGet();
        return userRepository.save(User.builder()
            .developerType(developerType)
            .name(nameBase + uniqueId)
            .nickname("Nickname" + uniqueId)
            .email(nameBase.toLowerCase() + uniqueId + "@example.com")
            .provider("google")
            .build());
    }

    public Map<Long, List<Study>> createStudiesForAllTypes(int typesCount, int studiesPerType) {
        Map<Long, List<Study>> studiesByType = new HashMap<>();
        DeveloperType developerType = createDeveloperType("Backend Developer");

        for (long typeId = 1; typeId <= typesCount; typeId++) {
            StudyType studyType = createStudyType("StudyType-" + typeId, 100L);
            List<Study> typeStudies = createConnectedStudies(studyType, developerType, studiesPerType);
            studiesByType.put(typeId, typeStudies);
        }

        return studiesByType;
    }

    public List<Study> createConnectedStudies(StudyType studyType, DeveloperType developerType, int count) {
        List<Study> studies = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Study study = Study.builder()
                .studyType(studyType)
                .developerType(developerType)
                .build();
            study.connect();
            studies.add(study);
        }
        return studyRepository.saveAll(studies);
    }

    public UserStudy createUserStudy(User user, Study study, boolean completed, LocalDateTime completedAt) {
        UserStudy userStudy = UserStudy.builder()
            .user(user)
            .study(study)
            .scheduledAt(LocalDateTime.now().minusDays(1))
            .build();

        if (completed) {
            userStudy.complete();
            ReflectionTestUtils.setField(userStudy, "completedAt", completedAt);
        }

        return userStudyRepository.save(userStudy);
    }

    public User setupTestUserWithStudies(Long userId, Map<Long, List<Study>> studiesByType,
                                         LocalDateTime completionTime) {
        DeveloperType devType = studiesByType.values().stream()
            .flatMap(List::stream)
            .findFirst()
            .map(Study::getDeveloperType)
            .orElseThrow(() -> new IllegalStateException("No studies available"));

        User user = createUniqueUser(devType, "User" + userId + "_");

        for (long typeId = 1; typeId <= 2; typeId++) {
            if (!studiesByType.containsKey(typeId) || studiesByType.get(typeId).isEmpty()) {
                throw new IllegalStateException("Missing studies for type ID " + typeId);
            }
            Study study = studiesByType.get(typeId).getFirst();
            createUserStudy(user, study, true, completionTime);
        }

        for (long typeId = 3; typeId <= 4; typeId++) {
            if (!studiesByType.containsKey(typeId) || studiesByType.get(typeId).isEmpty()) {
                throw new IllegalStateException("Missing studies for type ID " + typeId);
            }
            Study study = studiesByType.get(typeId).getFirst();
            createUserStudy(user, study, false, null);
        }

        return user;
    }

    public List<User> setupMultipleUsersWithStudies(Long[] userIds, Map<Long, List<Study>> studiesByType,
                                                    LocalDateTime completionTime) {
        List<User> users = new ArrayList<>();
        for (Long userId : userIds) {
            users.add(setupTestUserWithStudies(userId, studiesByType, completionTime));
        }
        return users;
    }
}
