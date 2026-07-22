package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_EMAIL;

@Service
@Slf4j
public class AppellantCompleteCaseReviewStatutoryTimeframe24WeeksLrContentPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String templateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;
    private final String nonAdaPrefix;


    public AppellantCompleteCaseReviewStatutoryTimeframe24WeeksLrContentPersonalisationEmail(
            @Value("${govnotify.template.completeCaseReviewStatutoryTimeframe24Weeks.legalRep.email}") String templateId,
            @Value("${govnotify.emailPrefix.nonAda}") String nonAdaPrefix,
            RecipientsFinder recipientsFinder,
            CustomerServicesProvider customerServicesProvider) {
        this.templateId = templateId;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.nonAdaPrefix = nonAdaPrefix;
    }


    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_EMAIL;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return Stf24WeeksUtil.buildParams(asylumCase, customerServicesProvider, nonAdaPrefix, true);

    }


}
