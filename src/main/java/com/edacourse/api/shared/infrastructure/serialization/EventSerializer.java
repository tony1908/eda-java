package com.edacourse.api.shared.infrastructure.serialization;

public interface EventSerializer {
    String serialize(Object event);
    <T> T deserialize(String data, Class<T> type);
}
