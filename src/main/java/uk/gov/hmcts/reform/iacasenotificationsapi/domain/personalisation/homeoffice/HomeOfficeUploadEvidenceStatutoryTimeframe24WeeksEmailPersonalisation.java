package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.*;

@Slf4j
@Service
public class HomeOfficeUploadEvidenceStatutoryTimeframe24WeeksEmailPersonalisation implements EmailNotificationPersonalisation {
    private final String templateID;


    private final String nonAdaPrefix;
    private final String iaExUiFrontendUrl;
    private final String apcPrivateHomeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;

    public HomeOfficeUploadEvidenceStatutoryTimeframe24WeeksEmailPersonalisation(
            @Value("${apcPrivateHomeOfficeEmailAddress}") String apcPrivateHomeOfficeEmailAddress,
            @Value(STF_24_WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL_TEMPLATE) String templateID,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            @Value("${govnotify.emailPrefix.nonAda}") String nonAdaPrefix, CustomerServicesProvider customerServicesProvider) {
        this.templateID = templateID;

        this.nonAdaPrefix = nonAdaPrefix;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.apcPrivateHomeOfficeEmailAddress = apcPrivateHomeOfficeEmailAddress;
        this.customerServicesProvider = customerServicesProvider;
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
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return Stf24WeeksUtil.buildCommonParams(Stf24WeeksUtil.Stf24WeeksNotificationFor.HOME_OFFICE, asylumCase, customerServicesProvider)
                .put(SUBJECT_PREFIX_KEY, nonAdaPrefix).put(LINK_TO_ONLINE_SERVICE_KEY, iaExUiFrontendUrl).build();
    }

}
