package se.sowl.devlydomain.prompt.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudyPromptTest {

    @Test
    void createStudyPrompt() {
        Long developerTypeId = 1L;
        Long studyTypeId = 2L;
        String generateContent = "You are a backend developer expert. Generate Java Spring related terms in JSON format.";

        StudyPrompt studyPrompt = new StudyPrompt(developerTypeId, studyTypeId, generateContent);

        assertThat(studyPrompt.getDeveloperTypeId()).isEqualTo(developerTypeId);
        assertThat(studyPrompt.getStudyTypeId()).isEqualTo(studyTypeId);
        assertThat(studyPrompt.getGenerateContent()).isEqualTo(generateContent);
    }

    @Test
    void createEmptyStudyPrompt() {
        StudyPrompt studyPrompt = new StudyPrompt();
        
        assertThat(studyPrompt).isNotNull();
        assertThat(studyPrompt.getDeveloperTypeId()).isNull();
        assertThat(studyPrompt.getStudyTypeId()).isNull();
        assertThat(studyPrompt.getGenerateContent()).isNull();
    }
}