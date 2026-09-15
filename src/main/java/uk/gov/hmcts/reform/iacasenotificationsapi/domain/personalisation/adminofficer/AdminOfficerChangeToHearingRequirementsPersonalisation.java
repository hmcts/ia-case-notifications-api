package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.hasStf24WeeksStatus;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@Service
public class AdminOfficerChangeToHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {

    private final String changeToHearingRequirementsAdminOfficerTemplateId;
    private final String changeToHearingRequirementsAdminHearingCenterTemplateId;
    private final String reviewHearingRequirementsAdminOfficerEmailAddress;
    private final AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;
    private final EmailAddressFinder emailAddressFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AdminOfficerChangeToHearingRequirementsPersonalisation(
        @NotNull(message = "changeToHearingRequirementsAdminOfficerTemplateId cannot be null") @Value("${govnotify.template.changeToHearingRequirements.adminOfficer.email}") String changeToHearingRequirementsAdminOfficerTemplateId,
        @NotNull(message = "changeToHearingRequirementsAdminHearingCenterTemplateId cannot be null") @Value("${govnotify.template.changeToHearingRequirements.adminHearingCenter24Weeks.email}") String changeToHearingRequirementsAdminHearingCenterTemplateId,
        @Value("${reviewHearingRequirementsAdminOfficerEmailAddress}") String reviewHearingRequirementsAdminOfficerEmailAddress,
        AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider,
        EmailAddressFinder emailAddressFinder
    ) {
        this.changeToHearingRequirementsAdminOfficerTemplateId = changeToHearingRequirementsAdminOfficerTemplateId;
        this.changeToHearingRequirementsAdminHearingCenterTemplateId = changeToHearingRequirementsAdminHearingCenterTemplateId;
        this.reviewHearingRequirementsAdminOfficerEmailAddress = reviewHearingRequirementsAdminOfficerEmailAddress;
        this.adminOfficerPersonalisationProvider = adminOfficerPersonalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CHANGE_TO_HEARING_REQUIREMENTS_ADMIN_OFFICER";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return hasStf24WeeksStatus(asylumCase) ? changeToHearingRequirementsAdminHearingCenterTemplateId : changeToHearingRequirementsAdminOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (hasStf24WeeksStatus(asylumCase)) {
            return Collections.singleton(emailAddressFinder.getAdminHearingCenterEmailAddress(asylumCase));
        } else {
            return Collections.singleton(reviewHearingRequirementsAdminOfficerEmailAddress);
        }
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase))
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix);

        return listCaseFields.build();
    }
}
