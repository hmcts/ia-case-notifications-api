package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_HOME_OFFICE_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STF_24_WEEKS_HEARING_REQUIREMENTS_EMAIL_TEMPLATE;

@Slf4j
@Service
public class HomeOfficeHearingRequirementsStf24WeeksEmailPersonalisation implements EmailNotificationPersonalisation {
    private final String templateID;


    private final String nonAdaPrefix;
    private final String iaExUiFrontendUrl;
    private final String apcPrivateHomeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;

    public HomeOfficeHearingRequirementsStf24WeeksEmailPersonalisation(
            @Value("${apcPrivateHomeOfficeEmailAddress}") String apcPrivateHomeOfficeEmailAddress,
            @Value(STF_24_WEEKS_HEARING_REQUIREMENTS_EMAIL_TEMPLATE) String templateID,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            @Value("${govnotify.emailPrefix.nonAda}") String nonAdaPrefix, CustomerServicesProvider customerServicesProvider, DateTimeExtractor dateTimeExtractor, HearingDetailsFinder hearingDetailsFinder) {
        this.templateID = templateID;

        this.nonAdaPrefix = nonAdaPrefix;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.apcPrivateHomeOfficeEmailAddress = apcPrivateHomeOfficeEmailAddress;
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
        return Collections.singleton(apcPrivateHomeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_HOME_OFFICE_EMAIL;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return Stf24WeeksUtil.buildHearingRequirementsParams(Stf24WeeksUtil.Stf24WeeksNotificationFor.HOME_OFFICE, asylumCase, nonAdaPrefix, iaExUiFrontendUrl, customerServicesProvider, dateTimeExtractor, hearingDetailsFinder);
    }

}
