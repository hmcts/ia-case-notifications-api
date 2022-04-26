package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailEmailNotificationPersonalisation;

@Service
public class HomeOfficeBailApplicationSubmittedPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeBailApplicationSubmittedPersonalisationTemplateId;
    private final String alarAppealsBailTeamHomeOfficeEmailAddress;


    public HomeOfficeBailApplicationSubmittedPersonalisation(
        @NotNull(message = "homeOfficeBailApplicationSubmittedPersonalisationTemplateId cannot be null")
        @Value("${govnotify.template.bail.submitApplication.email}") String homeOfficeBailApplicationSubmittedPersonalisationTemplateId,
        @Value("${alarAppealsBailTeamHomeOfficeEmailAddress}") String alarAppealsBailTeamHomeOfficeEmailAddress
    ) {
        this.homeOfficeBailApplicationSubmittedPersonalisationTemplateId = homeOfficeBailApplicationSubmittedPersonalisationTemplateId;
        this.alarAppealsBailTeamHomeOfficeEmailAddress = alarAppealsBailTeamHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_SUBMITTED_HOME_OFFICE";
    }

    @Override
    public String getTemplateId() {
        return homeOfficeBailApplicationSubmittedPersonalisationTemplateId;
    }



    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(alarAppealsBailTeamHomeOfficeEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");
        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .build();
    }
}
