package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.LegalRepresentativeBailEmailNotificationPersonalisation;

import java.util.Map;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

@Service
public class LegalRepresentativeBailApplicationEditedDisposalPersonalisationEmail implements LegalRepresentativeBailEmailNotificationPersonalisation {

    private final String bailApplicationEditedDisposalLegalRepresentativeTemplateId;

    public LegalRepresentativeBailApplicationEditedDisposalPersonalisationEmail(
        @NotNull(message = "bailApplicationEditedDisposalLegalRepresentativeTemplateId cannot be null")
        @Value("${govnotify.bail.template.editApplication.disposal.email}") String bailApplicationEditedDisposalLegalRepresentativeTemplateId
    ) {
        this.bailApplicationEditedDisposalLegalRepresentativeTemplateId = bailApplicationEditedDisposalLegalRepresentativeTemplateId;
    }

    @Override
    public String getTemplateId() {
        return bailApplicationEditedDisposalLegalRepresentativeTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_EDITED_DISPOSAL_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .build();
    }
}
