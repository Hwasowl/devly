package se.sowl.devlybatch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.study.domain.Study;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;
import se.sowl.devlydomain.user.domain.User;

import java.util.List;

@Sql(scripts = {
    "/org/springframework/batch/core/schema-drop-h2.sql",
    "/org/springframework/batch/core/schema-h2.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class MediumBatchTest {
    protected JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    protected JobLauncher jobLauncher;

    @Autowired
    protected Job wordCreationJob;

    @Autowired
    protected Job prCreationJob;

    @Autowired
    protected Job studyCreationJob;

    @Autowired
    protected JobRepository jobRepository;

    @Autowired
    protected StudyTypeRepository studyTypeRepository;

    @Autowired
    protected StudyRepository studyRepository;

    @Autowired
    protected DeveloperTypeRepository developerTypeRepository;

    protected List<DeveloperType> getDeveloperTypes() {
        DeveloperType frontEnd = new DeveloperType("Backend Developer");
        DeveloperType backEnd = new DeveloperType("Frontend Developer");
        return List.of(frontEnd, backEnd);
    }

    protected List<StudyType> getStudyTypes() {
        StudyType word = new StudyType("word", 100L);
        StudyType knowledge = new StudyType("knowledge", 150L);
        StudyType pr = new StudyType("pr", 300L);
        StudyType discussion = new StudyType("discussion", 300L);
        return List.of(word, knowledge, pr, discussion);
    }

    protected User createUser(Long id, DeveloperType developerType, String name, String nickname, String email, String provider) {
        return User.builder()
            .id(id)
            .name(name)
            .developerType(developerType)
            .nickname(nickname)
            .email(email)
            .provider(provider)
            .build();
    }

    protected Study buildStudy(StudyType studyType, DeveloperType developerType) {
        return Study.builder()
            .studyType(studyType)
            .developerType(developerType)
            .build();
    }
}
