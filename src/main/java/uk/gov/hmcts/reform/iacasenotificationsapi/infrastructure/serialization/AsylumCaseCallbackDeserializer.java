package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.serialization;

import tools.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

@Component
public class AsylumCaseCallbackDeserializer implements Deserializer<Callback<AsylumCase>> {

    private final JsonMapper mapper;

    public AsylumCaseCallbackDeserializer(
            JsonMapper mapper
    ) {
        this.mapper = mapper;
    }

    public Callback<AsylumCase> deserialize(
        String source
    ) {
        try {

            return mapper.readValue(
                source,
                new TypeReference<Callback<AsylumCase>>() {
                }
            );

        } catch (JacksonException e) {
            throw new IllegalArgumentException("Could not deserialize callback", e);
        }
    }
}
