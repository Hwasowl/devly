package se.sowl.devlydomain.discussion.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscussionStatusTest {

    @Test
    @DisplayName("DiscussionStatus enum이 올바른 값들을 가져야 한다")
    void discussionStatusShouldHaveCorrectValues() {
        DiscussionStatus[] values = DiscussionStatus.values();
        
        assertEquals(4, values.length);
        assertTrue(containsValue(values, DiscussionStatus.READY));
        assertTrue(containsValue(values, DiscussionStatus.IN_PROGRESS));
        assertTrue(containsValue(values, DiscussionStatus.COMPLETED));
        assertTrue(containsValue(values, DiscussionStatus.FAILED));
    }

    @Test
    @DisplayName("각 enum 값이 올바른 설명을 가져야 한다")
    void enumValuesShouldHaveCorrectDescriptions() {
        assertEquals("준비 완료", DiscussionStatus.READY.getDescription());
        assertEquals("진행 중", DiscussionStatus.IN_PROGRESS.getDescription());
        assertEquals("완료", DiscussionStatus.COMPLETED.getDescription());
        assertEquals("실패", DiscussionStatus.FAILED.getDescription());
    }

    @Test
    @DisplayName("valueOf()로 올바른 enum 값을 가져올 수 있어야 한다")
    void valueOfShouldReturnCorrectEnum() {
        assertEquals(DiscussionStatus.READY, DiscussionStatus.valueOf("READY"));
        assertEquals(DiscussionStatus.IN_PROGRESS, DiscussionStatus.valueOf("IN_PROGRESS"));
        assertEquals(DiscussionStatus.COMPLETED, DiscussionStatus.valueOf("COMPLETED"));
        assertEquals(DiscussionStatus.FAILED, DiscussionStatus.valueOf("FAILED"));
    }

    @Test
    @DisplayName("valueOf()에 잘못된 값을 전달하면 예외가 발생해야 한다")
    void valueOfShouldThrowExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, 
            () -> DiscussionStatus.valueOf("INVALID_STATUS"));
    }

    @Test
    @DisplayName("enum 값들이 동등성 비교에서 올바르게 작동해야 한다")
    void enumEqualityShouldWork() {
        DiscussionStatus status1 = DiscussionStatus.READY;
        DiscussionStatus status2 = DiscussionStatus.READY;
        DiscussionStatus status3 = DiscussionStatus.COMPLETED;
        
        assertEquals(status1, status2);
        assertNotEquals(status1, status3);
        assertTrue(status1 == status2); // enum은 == 비교가 가능
        assertFalse(status1 == status3);
    }

    private boolean containsValue(DiscussionStatus[] values, DiscussionStatus target) {
        for (DiscussionStatus value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}