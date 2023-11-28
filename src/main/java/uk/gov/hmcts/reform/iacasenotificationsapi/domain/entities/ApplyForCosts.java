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
    private String applyForCostsCreationDate;

    public ApplyForCosts() {
        // noop -- for deserializer
    }

    public ApplyForCosts(String appliedCostsType, String respondentToCostsOrder, String applyForCostsApplicantType, String applyForCostsCreationDate) {
        requireNonNull(appliedCostsType);
        requireNonNull(respondentToCostsOrder);
        requireNonNull(applyForCostsApplicantType);
        requireNonNull(applyForCostsCreationDate);
        this.appliedCostsType = appliedCostsType;
        this.respondentToCostsOrder = respondentToCostsOrder;
        this.applyForCostsApplicantType = applyForCostsApplicantType;
        this.applyForCostsCreationDate = applyForCostsCreationDate;
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

    public String getApplyForCostsCreationDate() {
        requireNonNull(applyForCostsCreationDate);
        return applyForCostsCreationDate;
    }

}
