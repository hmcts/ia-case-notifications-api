package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum AppealType {

    RP("revocationOfProtection"),
    PA("protection"),
    EA("refusalOfEu"),
    HU("refusalOfHumanRights"),
    DC("deprivation"),
    EU("euSettlementScheme");

    @JsonValue
    private String value;

    AppealType(String value) {
        this.value = value;
    }

    public static Optional<AppealType> from(
            String value
    ) {
        return stream(values())
                .filter(v -> v.getValue().equals(value))
                .findFirst();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
