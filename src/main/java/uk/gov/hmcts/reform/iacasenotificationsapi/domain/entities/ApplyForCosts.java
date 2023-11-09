package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplyForCosts {
    private String appliedCostsType;
    private String respondentToCostsOrder;

    public ApplyForCosts() {
        // noop -- for deserializer
    }

    public ApplyForCosts(String appliedCostsType, String respondentToCostsOrder) {
        requireNonNull(appliedCostsType);
        requireNonNull(respondentToCostsOrder);
        this.appliedCostsType = appliedCostsType;
        this.respondentToCostsOrder = respondentToCostsOrder;
    }

    public String getAppliedCostsType() {
        requireNonNull(appliedCostsType);
        return appliedCostsType;
    }

    public String getRespondentToCostsOrder() {
        requireNonNull(respondentToCostsOrder);
        return respondentToCostsOrder;
    }

}
