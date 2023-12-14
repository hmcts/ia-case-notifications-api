package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ApplyForCosts {
    private String applyForCostsApplicantType;
    private String applyForCostsRespondentRole;
    private String respondentToCostsOrder;
    private String loggedUserRole;
    private String appliedCostsType;
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
}
