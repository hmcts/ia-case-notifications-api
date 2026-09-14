package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.LegalRepresentativeEmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Map;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STF_24_WEEKS_HEARING_REQUIREMENTS_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.Stf24WeeksNotificationFor.LEGAL_REPRESENTATIVE;

@Service
public class LegalRepHearingRequirementsStf24WeeksEmailPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {
    private final String templateID;

    private final String nonAdaPrefix;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;

    public LegalRepHearingRequirementsStf24WeeksEmailPersonalisation(
            @Value(STF_24_WEEKS_HEARING_REQUIREMENTS_EMAIL_TEMPLATE) String templateID,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            @Value("${govnotify.emailPrefix.nonAda}") String nonAdaPrefix,
            CustomerServicesProvider customerServicesProvider, DateTimeExtractor dateTimeExtractor, HearingDetailsFinder hearingDetailsFinder
    ) {
        this.templateID = templateID;

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
    public String getReferenceId(Long caseId) {
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_EMAIL;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return Stf24WeeksUtil.buildHearingRequirementsParams(LEGAL_REPRESENTATIVE, asylumCase, nonAdaPrefix, iaExUiFrontendUrl, customerServicesProvider, dateTimeExtractor, hearingDetailsFinder);
    }

}
