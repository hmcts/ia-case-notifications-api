package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.roleassignment;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum GrantType {
    BASIC, SPECIFIC, STANDARD, CHALLENGED, EXCLUDED, @JsonEnumDefaultValue UNKNOWN
}
