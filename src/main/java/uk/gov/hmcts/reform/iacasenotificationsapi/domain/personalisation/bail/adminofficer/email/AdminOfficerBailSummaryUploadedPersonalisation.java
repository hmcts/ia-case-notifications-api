package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.adminofficer.email;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class AdminOfficerBailSummaryUploadedPersonalisation implements BailEmailNotificationPersonalisation {

    private final String bailSummaryUploadedAdminOfficerTemplateId;
    private final EmailAddressFinder emailAddressFinder;

    public AdminOfficerBailSummaryUploadedPersonalisation(
            @NotNull(message = "bailSummaryUploadedAdminOfficerTemplateId cannot be null")
            @Value("${govnotify.template.bail.uploadBailSummary.email}") String bailSummaryUploadedAdminOfficerTemplateId,
            EmailAddressFinder emailAddressFinder
    ) {
        this.bailSummaryUploadedAdminOfficerTemplateId = bailSummaryUploadedAdminOfficerTemplateId;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return bailSummaryUploadedAdminOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(emailAddressFinder.getBailHearingCentreEmailAddress(bailCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_SUMMARY_UPLOADED_ADMIN_OFFICER";
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
