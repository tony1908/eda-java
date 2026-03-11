package com.edacourse.solid;

public interface EventSerializer {
    String serialize(Object event);

    <T> T deserialize(String data, Class<T> type);
}
