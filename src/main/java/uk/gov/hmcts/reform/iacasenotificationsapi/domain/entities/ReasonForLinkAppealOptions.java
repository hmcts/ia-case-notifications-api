package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum ReasonForLinkAppealOptions {
    FAMILIAL("Familial"),
    SHARED_EVIDENCE("Shared evidence"),
    GUARDIAN("Guardian"),
    BAIL("Bail"),
    HOME_OFFICE_REQUEST("Home Office request"),
    OTHER_APPEAL_PENDING("Other appeal pending"),
    OTHER_APPEAL_DECIDED("Other appeal decided");

    @JsonValue
    private final String id;

    ReasonForLinkAppealOptions(String id) {
        this.id = id;
    }

}
