package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

@Service
public class RespondentAppellantFtpaSubmittedPersonalisation implements EmailNotificationPersonalisation {

    private final String applyForFtpaTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final EmailAddressFinder emailAddressFinder;

    @Value("${govnotify.emailPrefix.detained}")
    private String detainedPrefix;
    @Value("${govnotify.emailPrefix.nonDetained}")
    private String nonDetainedPrefix;

    public RespondentAppellantFtpaSubmittedPersonalisation(
        @Value("${govnotify.template.applyForFtpa.otherHomeOffice.email}") String applyForFtpaTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider,
        EmailAddressFinder emailAddressFinder
    ) {
        this.applyForFtpaTemplateId = applyForFtpaTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_APPELLANT_FTPA_SUBMITTED";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getTemplateId() {
        return applyForFtpaTemplateId;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation(callback))
            .put("subjectPrefix", isAppellantInDetention(callback.getCaseDetails().getCaseData())
                ? detainedPrefix
                : nonDetainedPrefix)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }
}
