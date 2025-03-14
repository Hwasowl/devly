package se.sowl.devlybatch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import se.sowl.devlydomain.developer.domain.DeveloperType;
import se.sowl.devlydomain.developer.repository.DeveloperTypeRepository;
import se.sowl.devlydomain.study.domain.StudyType;
import se.sowl.devlydomain.study.repository.StudyRepository;
import se.sowl.devlydomain.study.repository.StudyTypeRepository;

import java.util.List;

@Sql(scripts = {
    "/org/springframework/batch/core/schema-drop-h2.sql",
    "/org/springframework/batch/core/schema-h2.sql"
}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
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
}
