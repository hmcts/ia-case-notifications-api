package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.D_MMM_YYYY;


@Service
@Slf4j
public class HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation
        implements EmailNotificationPersonalisation {

    public static final String HOME_OFFICE_REFERENCE_NUMBER_KEY = "homeOfficeReferenceNumber";
    public static final int DAYS_14 = 14;
    public static final int DAYS_42 = 42;
    public static final int DAYS_56 = 56;
    private static final String REFERENCE_ID_SUFFIX = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL";
    private static final String SUBJECT_PREFIX_KEY = "subjectPrefix";
    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";
    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    private static final String LINK_TO_ONLINE_SERVICE_KEY = "linkToOnlineService";
    private static final String EMPTY_STRING = "";
    private final String templateId;
    private final String iaExUiFrontendUrl;
    private final String apcPrivateHomeOfficeEmailAddress;
    private final String nonAdaPrefix;
    private final CustomerServicesProvider customerServicesProvider;

    public HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation(
            @NotNull(message = "templateId cannot be null") @Value("${govnotify.template.completeCaseReviewStatutoryTimeframe24Weeks.homeOffice.email}") String templateId,
            @Value("${apcPrivateHomeOfficeEmailAddress}") String apcPrivateHomeOfficeEmailAddress,
            @Value("${govnotify.emailPrefix.nonAda}") String nonAdaPrefix,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl, CustomerServicesProvider customerServicesProvider) {
        this.templateId = templateId;
        this.apcPrivateHomeOfficeEmailAddress = apcPrivateHomeOfficeEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.nonAdaPrefix = nonAdaPrefix;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        Set<String> emails = Collections.singleton(apcPrivateHomeOfficeEmailAddress);
        log.info("HO Emails - case review {}", emails);
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
        return ImmutableMap.<String, String>builder()
                .put(SUBJECT_PREFIX_KEY, nonAdaPrefix)
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put(HOME_OFFICE_REFERENCE_NUMBER_KEY, asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put(APPEAL_REFERENCE_NUMBER_KEY, asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING))
                .put(APPELLANT_GIVEN_NAMES_KEY, asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING))
                .put(APPELLANT_FAMILY_NAME_KEY, asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING))
                .put(LINK_TO_ONLINE_SERVICE_KEY, iaExUiFrontendUrl)
                .put("appealReceivedDate", AsylumCaseUtils.getAppealReceivedDate(asylumCase))
                .put("decisionSentDate", AsylumCaseUtils.getHomeOfficeDecisionDate(asylumCase))
                .put("24WeeksDeadline", AsylumCaseUtils.populateSTF24wDate(asylumCase))
                .put("practiceDirection", now.format(DateTimeFormatter.ofPattern(D_MMM_YYYY)))
                .put("14DaysFromDateOfDirection", now.plusDays(DAYS_14).format(DateTimeFormatter.ofPattern(D_MMM_YYYY)))
                .put("42DaysFromDateOfDirection", now.plusDays(DAYS_42).format(DateTimeFormatter.ofPattern(D_MMM_YYYY)))
                .put("56DaysFromDateOfDirection", now.plusDays(DAYS_56).format(DateTimeFormatter.ofPattern(D_MMM_YYYY)))
                .build();
    }

}
