package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.EMPTY_STRING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.LEGAL_REP_REFERENCE_NUMBER_KEY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.getLegalRepReferenceNo;

@Service
@Slf4j
public class LegalRepresentativeRemoveStatutoryTimeframe24WeeksPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {


    private static final String SUBJECT_PREFIX_KEY = "subjectPrefix";
    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";
    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    private static final String LINK_TO_ONLINE_SERVICE_KEY = "linkToOnlineService";
    private static final String COMPLETE_CASE_REVIEW_DATE_KEY = "completeCaseReviewDate";
    private final String templateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final String nonAdaPrefix;


    public LegalRepresentativeRemoveStatutoryTimeframe24WeeksPersonalisation(
            @NotNull(message = "templateId cannot be null") @Value("${govnotify.template.removeStatutoryTimeframe24Weeks.legalRep.email}") String templateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl, CustomerServicesProvider customerServicesProvider,
            @Value("${govnotify.emailPrefix.nonAda}") String nonAdaPrefix) {
        this.templateId = templateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.nonAdaPrefix = nonAdaPrefix;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return templateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap.<String, String>builder().put(SUBJECT_PREFIX_KEY, nonAdaPrefix)
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
                .put(APPEAL_REFERENCE_NUMBER_KEY, asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING))
                .put(LEGAL_REP_REFERENCE_NUMBER_KEY, getLegalRepReferenceNo(asylumCase))
                .put(APPELLANT_GIVEN_NAMES_KEY, asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING))
                .put(APPELLANT_FAMILY_NAME_KEY, asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING))
                .put(COMPLETE_CASE_REVIEW_DATE_KEY, AsylumCaseUtils.getCompleteCasedReviewDate(asylumCase))
                .put(LINK_TO_ONLINE_SERVICE_KEY, iaExUiFrontendUrl).build();
    }

}
