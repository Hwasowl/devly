package se.sowl.devlydomain.level.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LevelTest {

    @Test
    @DisplayName("승급에 필요한 경험치 판별이 가능해야 한다.")
    void canPromote() {
        Level level = Level.builder()
            .level(1)
            .requiredExp(100)
            .build();

        assertTrue(level.canPromote(100));
        assertFalse(level.canPromote(99));
    }
}
