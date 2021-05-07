package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OutOfTimeDecisionType {

    IN_TIME("inTime"),
    APPROVED("approved"),
    REJECTED("rejected"),
    UNKNOWN("unknown");

    @JsonValue
    private final String value;

    OutOfTimeDecisionType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
