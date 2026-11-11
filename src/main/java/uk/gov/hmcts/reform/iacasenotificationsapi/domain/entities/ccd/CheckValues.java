package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonCreator;

import static java.util.Objects.requireNonNull;

import java.util.List;

public class CheckValues<T> {

    private List<T> values;

    @JsonCreator
    private CheckValues() {
        // noop -- for deserializer
    }

    public CheckValues(List<T> values) {
        requireNonNull(values);
        this.values = values;
    }

    public List<T> getValues() {
        requireNonNull(values);
        return List.copyOf(values);
    }
}
