package se.sowl.devlyexternal.common.gpt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sowl.devlydomain.prompt.domain.StudyPrompt;
import se.sowl.devlydomain.prompt.repository.StudyPromptRepository;
import se.sowl.devlyexternal.common.gpt.exception.PromptNotExistException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GptPromptManagerTest {

    @Mock
    private StudyPromptRepository studyPromptRepository;

    @InjectMocks
    private GptPromptManager gptPromptManager;

    private StudyPrompt testStudyPrompt;
    private Long developerTypeId;
    private Long studyTypeId;

    @BeforeEach
    void setUp() {
        developerTypeId = 1L;
        studyTypeId = 2L;
        testStudyPrompt = new StudyPrompt(
            developerTypeId,
            studyTypeId,
            "You are a backend developer expert. Generate Java Spring related terms in JSON format."
        );
    }

    @Test
    @DisplayName("완전한 프롬프트 생성 - 제외할 내용이 있는 경우")
    void buildCompletePrompt_withExcludeContents() {
        when(studyPromptRepository.findByDeveloperTypeIdAndStudyTypeId(developerTypeId, studyTypeId))
            .thenReturn(Optional.of(testStudyPrompt));

        String result = gptPromptManager.buildCompletePrompt(
            developerTypeId, 
            studyTypeId, 
            Arrays.asList("Spring", "Java")
        );

        assertThat(result).contains("다음 용어나 주제들은 제외 해주세요:");
        assertThat(result).contains("- Spring");
        assertThat(result).contains("- Java");
        assertThat(result).contains("You are a backend developer expert. Generate Java Spring related terms in JSON format.");
    }

    @Test
    @DisplayName("완전한 프롬프트 생성 - 제외할 내용이 없는 경우")
    void buildCompletePrompt_withoutExcludeContents() {
        when(studyPromptRepository.findByDeveloperTypeIdAndStudyTypeId(developerTypeId, studyTypeId))
            .thenReturn(Optional.of(testStudyPrompt));

        String result = gptPromptManager.buildCompletePrompt(
            developerTypeId, 
            studyTypeId, 
            Collections.emptyList()
        );

        assertThat(result).doesNotContain("다음 용어나 주제들은 제외 해주세요:");
        assertThat(result).contains("You are a backend developer expert. Generate Java Spring related terms in JSON format.");
    }

    @Test
    @DisplayName("프롬프트가 존재하지 않는 경우 예외 발생")
    void buildCompletePrompt_throwsException_whenPromptNotExists() {
        when(studyPromptRepository.findByDeveloperTypeIdAndStudyTypeId(developerTypeId, studyTypeId))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> gptPromptManager.buildCompletePrompt(
            developerTypeId, 
            studyTypeId, 
            Collections.emptyList()
        )).isInstanceOf(PromptNotExistException.class)
          .hasMessage("프롬프트 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("기본 프롬프트 추가 - StringBuilder 버전")
    void addBasePrompt() {
        when(studyPromptRepository.findByDeveloperTypeIdAndStudyTypeId(developerTypeId, studyTypeId))
            .thenReturn(Optional.of(testStudyPrompt));

        StringBuilder builder = new StringBuilder("시작: ");
        gptPromptManager.addBasePrompt(developerTypeId, studyTypeId, builder);

        assertThat(builder.toString()).isEqualTo("시작: You are a backend developer expert. Generate Java Spring related terms in JSON format.");
    }

    @Test
    @DisplayName("기본 프롬프트 추가 시 프롬프트가 존재하지 않으면 예외 발생")
    void addBasePrompt_throwsException_whenPromptNotExists() {
        when(studyPromptRepository.findByDeveloperTypeIdAndStudyTypeId(developerTypeId, studyTypeId))
            .thenReturn(Optional.empty());

        StringBuilder builder = new StringBuilder();

        assertThatThrownBy(() -> gptPromptManager.addBasePrompt(developerTypeId, studyTypeId, builder))
            .isInstanceOf(PromptNotExistException.class)
            .hasMessage("프롬프트 정보를 찾을 수 없습니다.");
    }
}