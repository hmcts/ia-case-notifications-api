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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType.SMS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_14;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_42;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_56;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.D_MMM_YYYY;

@Slf4j
@Service
public class Appellant24WeeksReviewSms implements SmsNotificationPersonalisation {
    private static final String REFERENCE_ID_SUFFIX = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS";
    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";

    private static final String LINK_TO_SERVICE_TEXT_AND_URL = "linkToServiceTextAndUrl";
    private static final String EMPTY_STRING = "";
    private final String smsTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public Appellant24WeeksReviewSms(
            @Value("${govnotify.template.completeCaseReviewStatutoryTimeframe24Weeks.appellant.sms}") String smsTemplateId,
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
        return contactSmsSet.isEmpty() ? recipientsFinder.findAll(asylumCase, SMS) : contactSmsSet;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + REFERENCE_ID_SUFFIX;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        LocalDate now = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(D_MMM_YYYY);
        String appealRef = asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING);
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
                .put(APPEAL_REFERENCE_NUMBER_KEY, appealRef)
                .put("24WeeksDeadline", AsylumCaseUtils.populateStatutoryTimeFrame24wDate(asylumCase))
                .put("14DaysFromDateOfDirection", now.plusDays(DAYS_14).format(dtf))
                .put("42DaysFromDateOfDirection", now.plusDays(DAYS_42).format(dtf))
                .put("56DaysFromDateOfDirection", now.plusDays(DAYS_56).format(dtf));
        boolean noLegalRepresentation = AsylumCaseUtils.noLegalRepresentation(asylumCase);
        log.info("no legal representation? {}", noLegalRepresentation);
        if (noLegalRepresentation) {
            builder.put(LINK_TO_SERVICE_TEXT_AND_URL, "Sign into your account to see appeal: " + iaAipFrontendUrl);
        } else {
            builder.put(LINK_TO_SERVICE_TEXT_AND_URL, "");
        }

        return builder.build();
    }

}
