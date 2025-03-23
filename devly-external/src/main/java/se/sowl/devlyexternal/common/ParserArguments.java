package se.sowl.devlyexternal.common;

import java.util.HashMap;
import java.util.Map;

public class ParserArguments {
    private final Map<String, Object> parameters = new HashMap<>();

    public ParserArguments add(String key, Object value) {
        parameters.put(key, value);
        return this;
    }

    public <T> T get(String key, Class<T> type) {
        Object value = parameters.get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }
}
