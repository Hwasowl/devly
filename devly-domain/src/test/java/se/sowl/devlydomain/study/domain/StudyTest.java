package se.sowl.devlydomain.study.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.sowl.devlydomain.developer.domain.DeveloperType;

import static org.junit.jupiter.api.Assertions.*;

class StudyTest {

    @Test
    @DisplayName("스터디 생성 시 기본 상태는 UNCONNECTED여야 한다")
    void defaultStatusShouldBeUnconnected() {
        StudyType studyType = StudyType.builder().name("Test Study").build();
        DeveloperType developerType = DeveloperType.builder().name("BACKEND").build();
        
        Study study = Study.builder()
            .studyType(studyType)
            .developerType(developerType)
            .build();

        assertEquals(StudyStatus.UNCONNECTED, study.getStatus());
        assertFalse(study.isConnected());
    }

    @Test
    @DisplayName("UNCONNECTED 상태에서 connect() 호출 시 CONNECTED 상태로 변경되어야 한다")
    void connectFromUnconnectedState() {
        StudyType studyType = StudyType.builder().name("Test Study").build();
        DeveloperType developerType = DeveloperType.builder().name("BACKEND").build();
        
        Study study = Study.builder()
            .studyType(studyType)
            .developerType(developerType)
            .build();

        study.connect();

        assertEquals(StudyStatus.CONNECTED, study.getStatus());
        assertTrue(study.isConnected());
    }

    @Test
    @DisplayName("이미 CONNECTED 상태에서 connect() 호출 시 예외가 발생해야 한다")
    void connectFromConnectedStateShouldThrowException() {
        StudyType studyType = StudyType.builder().name("Test Study").build();
        DeveloperType developerType = DeveloperType.builder().name("BACKEND").build();
        
        Study study = Study.builder()
            .studyType(studyType)
            .developerType(developerType)
            .build();
        
        study.connect(); // 먼저 연결

        IllegalStateException exception = assertThrows(IllegalStateException.class, study::connect);
        assertEquals("Study is already connected", exception.getMessage());
    }

    @Test
    @DisplayName("CONNECTED 상태에서 disconnect() 호출 시 UNCONNECTED 상태로 변경되어야 한다")
    void disconnectFromConnectedState() {
        StudyType studyType = StudyType.builder().name("Test Study").build();
        DeveloperType developerType = DeveloperType.builder().name("BACKEND").build();
        
        Study study = Study.builder()
            .studyType(studyType)
            .developerType(developerType)
            .build();
        
        study.connect(); // 먼저 연결
        study.disconnect(); // 연결 해제

        assertEquals(StudyStatus.UNCONNECTED, study.getStatus());
        assertFalse(study.isConnected());
    }

    @Test
    @DisplayName("이미 UNCONNECTED 상태에서 disconnect() 호출 시 예외가 발생해야 한다")
    void disconnectFromUnconnectedStateShouldThrowException() {
        StudyType studyType = StudyType.builder().name("Test Study").build();
        DeveloperType developerType = DeveloperType.builder().name("BACKEND").build();
        
        Study study = Study.builder()
            .studyType(studyType)
            .developerType(developerType)
            .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, study::disconnect);
        assertEquals("Study is already disconnected", exception.getMessage());
    }

    @Test
    @DisplayName("모든 필수 필드가 있을 때 isValid()는 true를 반환해야 한다")
    void isValidShouldReturnTrueWhenAllFieldsArePresent() {
        StudyType studyType = StudyType.builder().name("Test Study").build();
        DeveloperType developerType = DeveloperType.builder().name("BACKEND").build();
        
        Study study = Study.builder()
            .studyType(studyType)
            .developerType(developerType)
            .build();

        assertTrue(study.isValid());
    }

    @Test
    @DisplayName("studyType이 null일 때 isValid()는 false를 반환해야 한다")
    void isValidShouldReturnFalseWhenStudyTypeIsNull() {
        DeveloperType developerType = DeveloperType.builder().name("BACKEND").build();
        
        Study study = Study.builder()
            .studyType(null)
            .developerType(developerType)
            .build();

        assertFalse(study.isValid());
    }

    @Test
    @DisplayName("developerType이 null일 때 isValid()는 false를 반환해야 한다")
    void isValidShouldReturnFalseWhenDeveloperTypeIsNull() {
        StudyType studyType = StudyType.builder().name("Test Study").build();
        
        Study study = Study.builder()
            .studyType(studyType)
            .developerType(null)
            .build();

        assertFalse(study.isValid());
    }
}