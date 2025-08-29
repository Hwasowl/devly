package se.sowl.devlydomain.study.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudyStatusTest {

    @Test
    @DisplayName("StudyStatus enum이 UNCONNECTED와 CONNECTED 값을 가져야 한다")
    void studyStatusShouldHaveCorrectValues() {
        StudyStatus[] values = StudyStatus.values();
        
        assertEquals(2, values.length);
        assertTrue(containsValue(values, StudyStatus.UNCONNECTED));
        assertTrue(containsValue(values, StudyStatus.CONNECTED));
    }

    @Test
    @DisplayName("UNCONNECTED enum 값이 올바르게 정의되어야 한다")
    void unconnectedEnumShouldBeValid() {
        StudyStatus status = StudyStatus.UNCONNECTED;
        
        assertNotNull(status);
        assertEquals("UNCONNECTED", status.name());
    }

    @Test
    @DisplayName("CONNECTED enum 값이 올바르게 정의되어야 한다")
    void connectedEnumShouldBeValid() {
        StudyStatus status = StudyStatus.CONNECTED;
        
        assertNotNull(status);
        assertEquals("CONNECTED", status.name());
    }

    @Test
    @DisplayName("valueOf()로 올바른 enum 값을 가져올 수 있어야 한다")
    void valueOfShouldReturnCorrectEnum() {
        assertEquals(StudyStatus.UNCONNECTED, StudyStatus.valueOf("UNCONNECTED"));
        assertEquals(StudyStatus.CONNECTED, StudyStatus.valueOf("CONNECTED"));
    }

    @Test
    @DisplayName("valueOf()에 잘못된 값을 전달하면 예외가 발생해야 한다")
    void valueOfShouldThrowExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, 
            () -> StudyStatus.valueOf("INVALID_STATUS"));
    }

    @Test
    @DisplayName("enum 값들이 동등성 비교에서 올바르게 작동해야 한다")
    void enumEqualityShouldWork() {
        StudyStatus status1 = StudyStatus.UNCONNECTED;
        StudyStatus status2 = StudyStatus.UNCONNECTED;
        StudyStatus status3 = StudyStatus.CONNECTED;
        
        assertEquals(status1, status2);
        assertNotEquals(status1, status3);
        assertTrue(status1 == status2); // enum은 == 비교가 가능
        assertFalse(status1 == status3);
    }

    private boolean containsValue(StudyStatus[] values, StudyStatus target) {
        for (StudyStatus value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}