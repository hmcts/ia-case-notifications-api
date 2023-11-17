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
    private String applyForCostsApplicantType;

    public ApplyForCosts() {
        // noop -- for deserializer
    }

    public ApplyForCosts(String appliedCostsType, String respondentToCostsOrder, String applyForCostsApplicantType) {
        requireNonNull(appliedCostsType);
        requireNonNull(respondentToCostsOrder);
        requireNonNull(applyForCostsApplicantType);
        this.appliedCostsType = appliedCostsType;
        this.respondentToCostsOrder = respondentToCostsOrder;
        this.applyForCostsApplicantType = applyForCostsApplicantType;
    }

    public String getAppliedCostsType() {
        requireNonNull(appliedCostsType);
        return appliedCostsType;
    }

    public String getRespondentToCostsOrder() {
        requireNonNull(respondentToCostsOrder);
        return respondentToCostsOrder;
    }

    public String getApplyForCostsApplicantType() {
        requireNonNull(applyForCostsApplicantType);
        return applyForCostsApplicantType;
    }

}
