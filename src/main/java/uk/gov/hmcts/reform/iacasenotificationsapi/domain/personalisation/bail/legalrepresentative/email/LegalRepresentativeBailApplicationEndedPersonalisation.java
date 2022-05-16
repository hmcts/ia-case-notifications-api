package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.LegalRepresentativeBailEmailNotificationPersonalisation;

@Service
public class LegalRepresentativeBailApplicationEndedPersonalisation implements LegalRepresentativeBailEmailNotificationPersonalisation {

    private final String bailApplicationEndedLegalRepresentativeTemplateId;


    public LegalRepresentativeBailApplicationEndedPersonalisation(
        @NotNull(message = "bailApplicationEndedLegalRepresentativeTemplateId cannot be null")
        @Value("${govnotify.bail.template.endApplication.email}") String bailApplicationEndedLegalRepresentativeTemplateId
    ) {
        this.bailApplicationEndedLegalRepresentativeTemplateId = bailApplicationEndedLegalRepresentativeTemplateId;
    }

    @Override
    public String getTemplateId() {
        return bailApplicationEndedLegalRepresentativeTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_ENDED_LEGAL_REPRESENTATIVE";
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
            .put("outcomeOfApplication", bailCase.read(BailCaseFieldDefinition.OUTCOME_OF_APPLICATION, String.class).orElse(""))
            .put("reasonsOfOutcome", bailCase.read(BailCaseFieldDefinition.REASONS_OF_OUTCOME, String.class).orElse("No reason given"))
            .put("endApplicationDate", bailCase.read(BailCaseFieldDefinition.END_APPLICATION_DATE, String.class).orElse(""))
            .build();
    }

}
