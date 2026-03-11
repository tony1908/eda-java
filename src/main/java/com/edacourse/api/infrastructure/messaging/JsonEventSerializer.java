package com.edacourse.solid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonEventSerializer implements EventSerializer {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String serialize(Object event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar el evento", e);
        }
    }

    @Override
    public <T> T deserialize(String data, Class<T> type) {
        try {
            return mapper.readValue(data, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar el evento", e);
        }
    }
}
