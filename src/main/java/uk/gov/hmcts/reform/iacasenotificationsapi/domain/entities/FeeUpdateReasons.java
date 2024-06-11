package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FeeUpdateReasons {
    DECISION_TYPE_CHANGED("decisionTypeChanged"),
    APPEAL_NOT_VALID("appealNotValid"),
    FEE_REMISSION_CHANGED("feeRemissionChanged");

    @JsonValue
    private String value;

    FeeUpdateReasons(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
