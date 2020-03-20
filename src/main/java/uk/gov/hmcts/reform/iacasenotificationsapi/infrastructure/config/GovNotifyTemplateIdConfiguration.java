package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "govnotify.template")
public class GovNotifyTemplateIdConfiguration {

    @NotBlank
    private String endAppealHomeOfficeTemplateId;

    @NotBlank
    private String endAppealLegalRepresentativeTemplateId;

    @NotBlank
    private String hearingBundleReadyLegalRepTemplateId;

    @NotBlank
    private String hearingBundleReadyHomeOfficeTemplateId;

    @NotBlank
    private String legalRepresentativeNonStandardDirectionTemplateId;

    @NotBlank
    private String legalRepNonStandardDirectionOfHomeOfficeTemplateId;

    @NotBlank
    private String caseOfficerRequestHearingRequirementsTemplateId;

    @NotBlank
    private String submittedHearingRequirementsLegalRepTemplateId;

    @NotBlank
    private String submittedHearingRequirementsCaseOfficerTemplateId;

    @NotBlank
    private String uploadedAdditionalEvidenceTemplateId;

    @NotBlank
    private String uploadedAddendumEvidenceTemplateId;

    @NotBlank
    private String changeDirectionDueDateTemplateId;

    @NotBlank
    private String changeDirectionDueDateOfHomeOfficeTemplateId;

    @NotBlank
    private String applicationGrantedApplicant;

    @NotBlank
    private String applicationGrantedOtherParty;

    @NotBlank
    private String applicationGrantedAdmin;

    @NotBlank
    private String applicationPartiallyGrantedApplicant;

    @NotBlank
    private String applicationPartiallyGrantedOtherParty;

    @NotBlank
    private String applicationPartiallyGrantedAdmin;

    @NotBlank
    private String applicationNotAdmittedApplicant;

    @NotBlank
    private String applicationNotAdmittedOtherParty;

    @NotBlank
    private String applicationRefusedApplicant;

    @NotBlank
    private String applicationRefusedOtherParty;

    @NotBlank
    private String applicationReheardApplicant;

    @NotBlank
    private String applicationReheardOtherParty;

    @NotBlank
    private String applicationReheardCaseworker;

    @NotBlank
    private String applicationAllowed;

    @NotBlank
    private String applicationDismissed;

}
