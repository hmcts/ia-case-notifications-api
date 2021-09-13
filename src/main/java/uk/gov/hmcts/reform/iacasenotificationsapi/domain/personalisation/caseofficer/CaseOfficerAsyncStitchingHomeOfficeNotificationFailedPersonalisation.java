package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class CaseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisation implements EmailNotificationPersonalisation {

    private final String asyncStitchingHomeOfficeNotificationFailedTemplateId;
    private EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;
    private final FeatureToggler featureToggler;

    public CaseOfficerAsyncStitchingHomeOfficeNotificationFailedPersonalisation(
            @NotNull(message = "asyncStitchingHomeOfficeNotificationFailedTemplateId cannot be null")
            @Value("${govnotify.template.asyncStitchingHomeOfficeNotificationFailed.caseOfficer.email}") String asyncStitchingHomeOfficeNotificationFailedTemplateId,
            PersonalisationProvider personalisationProvider,
            EmailAddressFinder emailAddressFinder,
            FeatureToggler featureToggler) {
        this.asyncStitchingHomeOfficeNotificationFailedTemplateId = asyncStitchingHomeOfficeNotificationFailedTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
        this.featureToggler = featureToggler;
    }

    @Override
    public String getTemplateId() {
        return asyncStitchingHomeOfficeNotificationFailedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("tcw-notifications-feature", false)
                ? Collections.singleton(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase))
                : Collections.emptySet();
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_STITCHING_BUNDLE_HO_NOTIFICATION_FAILED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return this.personalisationProvider.getTribunalHeaderPersonalisation(asylumCase);
    }
}
