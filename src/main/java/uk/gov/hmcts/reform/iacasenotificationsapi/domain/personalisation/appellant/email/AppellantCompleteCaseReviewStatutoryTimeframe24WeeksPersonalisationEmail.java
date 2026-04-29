package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_14;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_42;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_56;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.D_MMM_YYYY;

@Service
@Slf4j
public class AppellantCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationEmail implements EmailNotificationPersonalisation {

    private static final String REFERENCE_ID_SUFFIX = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL";
    private static final String SUBJECT_PREFIX_KEY = "subjectPrefix";
    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";
    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    private static final String LINK_TO_SERVICE_KEY = "linkToService";
    private static final String EMPTY_STRING = "";

    private final String templateId;
    private final String iaAipFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final String nonAdaPrefix;


    public AppellantCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationEmail(
            @Value("${govnotify.template.completeCaseReviewStatutoryTimeframe24Weeks.appellant.email}") String templateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            @Value("${govnotify.emailPrefix.nonAda}") String nonAdaPrefix,
            CustomerServicesProvider customerServicesProvider) {
        this.templateId = templateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.nonAdaPrefix = nonAdaPrefix;
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Set<String> emails = AsylumCaseUtils.getApplicantEmail(asylumCase);
        log.info("getRecipientsList -> Appellant-emails {}", emails);
        return emails;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + REFERENCE_ID_SUFFIX;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        LocalDate now = LocalDate.now();
        String familyName = asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING);
        String givenNames = asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING);
        return ImmutableMap.<String, String>builder()

                .put(SUBJECT_PREFIX_KEY, nonAdaPrefix)
                .put(APPEAL_REFERENCE_NUMBER_KEY, asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING))
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put(APPELLANT_GIVEN_NAMES_KEY, givenNames)
                .put(APPELLANT_FAMILY_NAME_KEY, familyName)
                .put("appellantFullName", givenNames + " " + familyName)
                .put("appealReceivedDate", AsylumCaseUtils.getAppealReceivedDate(asylumCase))
                .put("decisionSentDate", AsylumCaseUtils.getHomeOfficeDecisionDate(asylumCase))
                .put("24WeeksDeadline", AsylumCaseUtils.populateStatutoryTimeFrame24wDate(asylumCase))
                .put("practiceDirection", now.format(DateTimeFormatter.ofPattern(D_MMM_YYYY)))
                .put("14DaysFromDateOfDirection", now.plusDays(DAYS_14).format(DateTimeFormatter.ofPattern(D_MMM_YYYY)))
                .put("42DaysFromDateOfDirection", now.plusDays(DAYS_42).format(DateTimeFormatter.ofPattern(D_MMM_YYYY)))
                .put("56DaysFromDateOfDirection", now.plusDays(DAYS_56).format(DateTimeFormatter.ofPattern(D_MMM_YYYY)))

                .put(LINK_TO_SERVICE_KEY, iaAipFrontendUrl)
                .build();
    }


}
