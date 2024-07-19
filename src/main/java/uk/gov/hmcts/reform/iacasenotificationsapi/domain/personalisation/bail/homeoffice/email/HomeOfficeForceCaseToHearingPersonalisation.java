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
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;

@Service
public class HomeOfficeForceCaseToHearingPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeEmailTemplateId;
    private final String homeOfficeEmailAddress;

    public HomeOfficeForceCaseToHearingPersonalisation(
        @NotNull(message = "bailDocumentDeletedTemplateId cannot be null")
        @Value("${govnotify.bail.template.forceCaseToHearing.respondent.email}")
            String homeOfficeEmailTemplateId,
        @Value("${bailHomeOfficeEmailAddress}")
             String bailHomeOfficeEmailAddress
    ) {
        this.homeOfficeEmailTemplateId = homeOfficeEmailTemplateId;
        this.homeOfficeEmailAddress = bailHomeOfficeEmailAddress;
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return homeOfficeEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase asylumCase) {
        return Collections.singleton(homeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_FORCE_CASE_TO_HEARING";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");
        return ImmutableMap.<String, String>builder()
            .put("bailReferenceNumber",
                bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference",
                bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber",
                bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .build();
    }
}
