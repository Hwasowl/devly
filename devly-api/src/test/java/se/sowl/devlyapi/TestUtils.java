package se.sowl.devlyapi;

import org.springframework.test.util.ReflectionTestUtils;

public class TestUtils {
    public static <T, ID> T reflectionSetId(T entity, ID id) {
        try {
            ReflectionTestUtils.setField(entity, "id", id);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("ID 주입 실패: " + entity.getClass().getSimpleName(), e);
        }
    }
}
