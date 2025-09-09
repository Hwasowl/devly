package se.sowl.devlydomain.prompt.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StudyPromptTest {

    @Test
    void createStudyPrompt() {
        Long developerTypeId = 1L;
        Long studyTypeId = 2L;
        String roleContent = "당신은 백엔드 개발자입니다.";
        String generateContent = "자바 스프링 관련 단어를 생성하세요.";

        StudyPrompt studyPrompt = new StudyPrompt(developerTypeId, studyTypeId, roleContent, generateContent);

        assertThat(studyPrompt.getDeveloperTypeId()).isEqualTo(developerTypeId);
        assertThat(studyPrompt.getStudyTypeId()).isEqualTo(studyTypeId);
        assertThat(studyPrompt.getRoleContent()).isEqualTo(roleContent);
        assertThat(studyPrompt.getGenerateContent()).isEqualTo(generateContent);
    }

    @Test
    void createEmptyStudyPrompt() {
        StudyPrompt studyPrompt = new StudyPrompt();
        
        assertThat(studyPrompt).isNotNull();
        assertThat(studyPrompt.getDeveloperTypeId()).isNull();
        assertThat(studyPrompt.getStudyTypeId()).isNull();
        assertThat(studyPrompt.getRoleContent()).isNull();
        assertThat(studyPrompt.getGenerateContent()).isNull();
    }
}