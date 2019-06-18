package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.util.HashMap;
import java.util.Optional;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;

public class AsylumCase extends HashMap<String, Object> implements CaseData {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AsylumCase() {
        objectMapper.registerModule(new Jdk8Module());
    }

    public <T> Optional<T> read(AsylumCaseDefinition asylumCaseDefinition) {

        Object o = this.get(asylumCaseDefinition.value());

        if (o == null) {
            return Optional.empty();
        }

        T value = objectMapper.convertValue(o, asylumCaseDefinition.getTypeReference());

        return Optional.of(value);
    }

    public <T> Optional<T> read(AsylumCaseDefinition asylumCaseDefinition, Class<T> type) {
        return this.read(asylumCaseDefinition);
    }

    public <T> void write(AsylumCaseDefinition asylumCaseDefinition, T value) {
        this.put(asylumCaseDefinition.value(), value);
    }

}
