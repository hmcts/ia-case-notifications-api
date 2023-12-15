package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

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
    private String loggedUserRole;
    private String appliedCostsType;
    private String applyForCostsCreationDate;

    public ApplyForCosts() {
        // noop -- for deserializer
    }

    public ApplyForCosts(String loggedUserRole, String appliedCostsType) {
        this.loggedUserRole = loggedUserRole;
        this.appliedCostsType = appliedCostsType;
    }

    public ApplyForCosts(String appliedCostsType, String applyForCostsRespondentRole, String applyForCostsApplicantType) {
        this.appliedCostsType = appliedCostsType;
        this.applyForCostsRespondentRole = applyForCostsRespondentRole;
        this.applyForCostsApplicantType = applyForCostsApplicantType;
    }

    public ApplyForCosts(String appliedCostsType, String applyForCostsRespondentRole, String applyForCostsApplicantType, String applyForCostsCreationDate) {
        this.appliedCostsType = appliedCostsType;
        this.applyForCostsRespondentRole = applyForCostsRespondentRole;
        this.applyForCostsApplicantType = applyForCostsApplicantType;
        this.applyForCostsCreationDate = applyForCostsCreationDate;
    }

}
