package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.serialization;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Component
public class StdSerializer<T> implements Serializer<T> {

    private final JsonMapper mapper;

    public StdSerializer(
            JsonMapper mapper
    ) {
        this.mapper = mapper;
    }

    public String serialize(
        T data
    ) {
        try {

            return mapper.writeValueAsString(data);

        } catch (JacksonException e) {
            throw new IllegalArgumentException("Could not serialize data", e);
        }
    }
}
