package se.sowl.devlybatch.common.gpt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import se.sowl.devlybatch.common.gpt.exception.GPTContentProcessingException;
import se.sowl.devlyexternal.client.gpt.dto.GPTResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Profile("test")
class GptContentProcessorTest {

    private static class TestGptContentProcessor extends GptContentProcessor<String> {
        @Override
        protected void parseEntity(Long studyId, String entry, List<String> contents) {
            contents.add(entry.trim());
        }
    }

    private TestGptContentProcessor processor;

    @Mock
    private GPTResponse mockResponse;

    @BeforeEach
    void setUp() {
        processor = new TestGptContentProcessor();
    }

    @Test
    @DisplayName("GPT 응답을 파싱하여 항목을 반환한다")
    void parseResponse() {
        // Given
        Long studyId = 123L;
        String responseContent = "Entry 1---Entry 2---Entry 3";
        when(mockResponse.getContent()).thenReturn(responseContent);

        // When
        List<String> result = processor.parseGPTResponse(mockResponse, studyId);

        // Then
        assertEquals(3, result.size());
        assertEquals("Entry 1", result.get(0));
        assertEquals("Entry 2", result.get(1));
        assertEquals("Entry 3", result.get(2));
    }

    @Test
    @DisplayName("GPT 응답이 null일 때 예외를 던진다")
    void throwErrorWhenNullResponse() {
        // Given
        Long studyId = 123L;

        // When & Then
        GPTContentProcessingException exception = assertThrows(
            GPTContentProcessingException.class,
            () -> processor.parseGPTResponse(null, studyId)
        );
        assertEquals("GPT response is null", exception.getMessage());
    }

    @Test
    @DisplayName("GPT 응답의 내용이 비어 있을 때 예외를 던진다")
    void throwErrorWhenEmptyContent() {
        // Given
        Long studyId = 123L;
        when(mockResponse.getContent()).thenReturn("");

        // When & Then
        GPTContentProcessingException exception = assertThrows(
            GPTContentProcessingException.class,
            () -> processor.parseGPTResponse(mockResponse, studyId)
        );
        assertEquals("GPT response content is empty", exception.getMessage());
    }

    @Test
    @DisplayName("GPT 응답에 항목이 없을 때 예외를 던진다")
    void throwErrorWhenGetEmptyGptResponse() {
        // Given
        Long studyId = 123L;
        when(mockResponse.getContent()).thenReturn("---");

        // When & Then
        GPTContentProcessingException exception = assertThrows(
            GPTContentProcessingException.class,
            () -> processor.parseGPTResponse(mockResponse, studyId)
        );
        assertEquals("No entries found in GPT response", exception.getMessage());
    }

    @Test
    @DisplayName("GPT 응답에서 유효한 항목을 파싱할 수 없을 때 예외를 던진다")
    void throwWhenInvalidEntryParse() {
        // Given
        Long studyId = 123L;
        GptContentProcessor<String> emptyProcessor = new GptContentProcessor<>() {
            @Override
            protected void parseEntity(Long studyId, String entry, List<String> contents) {
                // 어떤 항목도 추가하지 않음
            }
        };
        when(mockResponse.getContent()).thenReturn("Entry 1---Entry 2");

        // When & Then
        GPTContentProcessingException exception = assertThrows(
            GPTContentProcessingException.class,
            () -> emptyProcessor.parseGPTResponse(mockResponse, studyId)
        );
        assertEquals("Failed to parse any valid entities from GPT response", exception.getMessage());
    }

    @Test
    @DisplayName("프롬프트가 null인 상태로 요청 시 예외를 던진다")
    void throwErrorWhenNullRequestPrompt() {
        // When & Then
        GPTContentProcessingException exception = assertThrows(
            GPTContentProcessingException.class,
            () -> processor.createGPTRequest(null)
        );
        assertEquals("Prompt cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("프롬프트가 빈 문자열인 상태로 요청 시 예외를 던진다")
    void throwErrorWhenEmptySpaceStringRequest() {
        // When & Then
        GPTContentProcessingException exception = assertThrows(
            GPTContentProcessingException.class,
            () -> processor.createGPTRequest("  ")
        );
        assertEquals("Prompt cannot be null or empty", exception.getMessage());
    }
}
