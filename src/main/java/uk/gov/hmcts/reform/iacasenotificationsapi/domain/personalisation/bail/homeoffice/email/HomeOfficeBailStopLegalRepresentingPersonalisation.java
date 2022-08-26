package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailEmailNotificationPersonalisation;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class HomeOfficeBailStopLegalRepresentingPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeBailStopLegalRepresentingPersonalisationTemplateId;
    private final String bailHomeOfficeEmailAddress;


    public HomeOfficeBailStopLegalRepresentingPersonalisation(
        @NotNull(message = "homeOfficeBailStopLegalRepresentingPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.stopLegalRepresenting.homeOffice}") String homeOfficeBailStopLegalRepresentingPersonalisationTemplateId,
        @Value("${bailHomeOfficeEmailAddress}") String bailHomeOfficeEmailAddress
    ) {
        this.homeOfficeBailStopLegalRepresentingPersonalisationTemplateId = homeOfficeBailStopLegalRepresentingPersonalisationTemplateId;
        this.bailHomeOfficeEmailAddress = bailHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_STOP_LEGAL_REPRESENTING_HOME_OFFICE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return homeOfficeBailStopLegalRepresentingPersonalisationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailHomeOfficeEmailAddress);
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
            .put("legalRepName", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_NAME, String.class).orElse(""))
            .put("legalRepCompany", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_COMPANY, String.class).orElse(""))
            .put("legalRepEmail", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class).orElse(""))
            .build();
    }
}
