package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.*;

@Service
public class AppellantSubmittedHearingRequirementsStf24WeeksEmailPersonalisation implements EmailNotificationPersonalisation {
    private final String templateID;
    private final RecipientsFinder recipientsFinder;
    private final String nonAdaPrefix;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;

    public AppellantSubmittedHearingRequirementsStf24WeeksEmailPersonalisation(
            @Value(STF_24_WEEKS_HEARING_REQUIREMENTS_EMAIL_TEMPLATE) String templateID,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            @Value("${govnotify.emailPrefix.nonAda}") String nonAdaPrefix,
            RecipientsFinder recipientsFinder, CustomerServicesProvider customerServicesProvider, DateTimeExtractor dateTimeExtractor, HearingDetailsFinder hearingDetailsFinder
    ) {
        this.templateID = templateID;
        this.recipientsFinder = recipientsFinder;
        this.nonAdaPrefix = nonAdaPrefix;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }


    @Override
    public String getTemplateId() {
        return templateID;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_APPELLANT_EMAIL;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return buildHearingRequirementsEmailParams(Stf24WeeksNotificationFor.APPELLANT, asylumCase, nonAdaPrefix, iaExUiFrontendUrl, customerServicesProvider, dateTimeExtractor, hearingDetailsFinder);
    }


}
