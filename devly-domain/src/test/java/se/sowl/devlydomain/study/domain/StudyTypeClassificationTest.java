package se.sowl.devlydomain.study.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudyTypeClassificationTest {

    @Test
    @DisplayName("유효한 value로 enum을 찾을 수 있어야 한다")
    void fromValidValue() {
        assertEquals(StudyTypeClassification.WORD, StudyTypeClassification.fromValue("word"));
        assertEquals(StudyTypeClassification.KNOWLEDGE, StudyTypeClassification.fromValue("knowledge"));
        assertEquals(StudyTypeClassification.PULL_REQUEST, StudyTypeClassification.fromValue("pr"));
        assertEquals(StudyTypeClassification.DISCUSSION, StudyTypeClassification.fromValue("discussion"));
    }

    @Test
    @DisplayName("무효한 value로 enum을 찾으려 할 때 예외가 발생해야 한다")
    void fromInvalidValueShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> StudyTypeClassification.fromValue("invalid"));
        assertEquals("Unknown study type: invalid", exception.getMessage());
    }

    @Test
    @DisplayName("null value로 enum을 찾으려 할 때 예외가 발생해야 한다")
    void fromNullValueShouldThrowException() {
        assertThrows(IllegalArgumentException.class, 
            () -> StudyTypeClassification.fromValue(null));
    }

    @Test
    @DisplayName("빈 문자열로 enum을 찾으려 할 때 예외가 발생해야 한다")
    void fromEmptyValueShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> StudyTypeClassification.fromValue(""));
        assertEquals("Unknown study type: ", exception.getMessage());
    }

    @Test
    @DisplayName("모든 enum 값이 유효해야 한다")
    void allEnumValuesShouldBeValid() {
        assertTrue(StudyTypeClassification.WORD.isValid());
        assertTrue(StudyTypeClassification.KNOWLEDGE.isValid());
        assertTrue(StudyTypeClassification.PULL_REQUEST.isValid());
        assertTrue(StudyTypeClassification.DISCUSSION.isValid());
    }

    @Test
    @DisplayName("WORD enum의 속성값이 올바르게 설정되어야 한다")
    void wordEnumPropertiesShouldBeCorrect() {
        StudyTypeClassification word = StudyTypeClassification.WORD;
        
        assertEquals("word", word.getValue());
        assertEquals(5L, word.getRequiredCount());
        assertEquals(1L, word.getId());
    }

    @Test
    @DisplayName("KNOWLEDGE enum의 속성값이 올바르게 설정되어야 한다")
    void knowledgeEnumPropertiesShouldBeCorrect() {
        StudyTypeClassification knowledge = StudyTypeClassification.KNOWLEDGE;
        
        assertEquals("knowledge", knowledge.getValue());
        assertEquals(3L, knowledge.getRequiredCount());
        assertEquals(2L, knowledge.getId());
    }

    @Test
    @DisplayName("PULL_REQUEST enum의 속성값이 올바르게 설정되어야 한다")
    void pullRequestEnumPropertiesShouldBeCorrect() {
        StudyTypeClassification pr = StudyTypeClassification.PULL_REQUEST;
        
        assertEquals("pr", pr.getValue());
        assertEquals(1L, pr.getRequiredCount());
        assertEquals(3L, pr.getId());
    }

    @Test
    @DisplayName("DISCUSSION enum의 속성값이 올바르게 설정되어야 한다")
    void discussionEnumPropertiesShouldBeCorrect() {
        StudyTypeClassification discussion = StudyTypeClassification.DISCUSSION;
        
        assertEquals("discussion", discussion.getValue());
        assertEquals(1L, discussion.getRequiredCount());
        assertEquals(4L, discussion.getId());
    }

    @Test
    @DisplayName("모든 enum 값들이 고유한 ID를 가져야 한다")
    void allEnumsShouldHaveUniqueIds() {
        StudyTypeClassification[] values = StudyTypeClassification.values();
        
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getId(), values[j].getId(),
                    "ID가 중복됩니다: " + values[i] + "와 " + values[j]);
            }
        }
    }

    @Test
    @DisplayName("모든 enum 값들이 고유한 value를 가져야 한다")
    void allEnumsShouldHaveUniqueValues() {
        StudyTypeClassification[] values = StudyTypeClassification.values();
        
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i].getValue(), values[j].getValue(),
                    "Value가 중복됩니다: " + values[i] + "와 " + values[j]);
            }
        }
    }
}