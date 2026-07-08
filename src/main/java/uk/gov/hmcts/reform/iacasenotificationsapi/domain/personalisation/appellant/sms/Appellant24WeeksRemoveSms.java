package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType.SMS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.EMPTY_STRING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.WEEKS_DEADLINE;

@Slf4j
@Service
public class Appellant24WeeksRemoveSms implements SmsNotificationPersonalisation {

    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";

    private static final String LINK_TO_SERVICE_TEXT_AND_URL = "linkToServiceTextAndUrl";

    private final String smsTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public Appellant24WeeksRemoveSms(
            @Value("${govnotify.template.removeStatutoryTimeframe24Weeks.appellant.sms}") String smsTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder
    ) {
        this.smsTemplateId = smsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return smsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Set<String> contactSmsSet = recipientsFinder.findReppedAppellant(asylumCase, SMS);
        Set<String> smsDetails = contactSmsSet.isEmpty() ? recipientsFinder.findAll(asylumCase, SMS) : contactSmsSet;
        log.info("Recipients list for Appellant 24 Weeks Remove SMS notification: {}", smsDetails);
        return smsDetails;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        String appealRef = asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING);
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
                .put(APPEAL_REFERENCE_NUMBER_KEY, appealRef)
                .put(WEEKS_DEADLINE, AsylumCaseUtils.populateStatutoryTimeFrame24wDate(asylumCase));
        boolean noLegalRepresentation = AsylumCaseUtils.noLegalRepresentation(asylumCase);
        log.info("no legal representation? {}", noLegalRepresentation);
        if (noLegalRepresentation) {
            builder.put(LINK_TO_SERVICE_TEXT_AND_URL, "Sign into your account to see how your appeal is progressing: " + iaAipFrontendUrl);
        } else {
            builder.put(LINK_TO_SERVICE_TEXT_AND_URL, EMPTY_STRING);
        }
        return builder.build();
    }

}
