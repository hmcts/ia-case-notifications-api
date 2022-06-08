package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailEmailNotificationPersonalisation;

@Service
public class HomeOfficeBailApplicationEditedSubmittedPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeBailApplicationEditedSubmittedWithLegalRepPersonalisationTemplateId;
    private final String homeOfficeBailApplicationEditedSubmittedWithoutLegalRepPersonalisationTemplateId;
    private final String bailHomeOfficeEmailAddress;


    public HomeOfficeBailApplicationEditedSubmittedPersonalisation(
        @NotNull(message = "homeOfficeBailApplicationEditedSubmittedPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.submitEditedApplication.email}") String homeOfficeBailApplicationEditedSubmittedWithLegalRepPersonalisationTemplateId,
        @Value("${govnotify.bail.template.submitEditedApplicationWithoutLr.email}") String homeOfficeBailApplicationEditedSubmittedWithoutLegalRepPersonalisationTemplateId,
        @Value("${bailHomeOfficeEmailAddress}") String bailHomeOfficeEmailAddress
    ) {
        this.homeOfficeBailApplicationEditedSubmittedWithLegalRepPersonalisationTemplateId = homeOfficeBailApplicationEditedSubmittedWithLegalRepPersonalisationTemplateId;
        this.homeOfficeBailApplicationEditedSubmittedWithoutLegalRepPersonalisationTemplateId = homeOfficeBailApplicationEditedSubmittedWithoutLegalRepPersonalisationTemplateId;
        this.bailHomeOfficeEmailAddress = bailHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_EDITED_AND_SUBMITTED_HOME_OFFICE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES
            ? homeOfficeBailApplicationEditedSubmittedWithLegalRepPersonalisationTemplateId : homeOfficeBailApplicationEditedSubmittedWithoutLegalRepPersonalisationTemplateId;
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
            .build();
    }
}
