package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.serialization;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

@Component
public class AsylumCaseCallbackDeserializer implements Deserializer<Callback<AsylumCase>> {

    private final ObjectMapper mapper;

    public AsylumCaseCallbackDeserializer(
        ObjectMapper mapper
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
