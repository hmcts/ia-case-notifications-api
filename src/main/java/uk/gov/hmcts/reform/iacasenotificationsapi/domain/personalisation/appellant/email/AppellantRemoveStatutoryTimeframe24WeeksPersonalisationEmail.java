package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.INTERNAL_APPELLANT_EMAIL;


@Service
@Slf4j
public class AppellantRemoveStatutoryTimeframe24WeeksPersonalisationEmail implements EmailNotificationPersonalisation {

    private static final String REFERENCE_ID_SUFFIX = "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL";
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


    public AppellantRemoveStatutoryTimeframe24WeeksPersonalisationEmail(
            @Value("${govnotify.template.removeStatutoryTimeframe24Weeks.appellant.email}") String templateId,
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
        Set<String> emails = asylumCase.read(INTERNAL_APPELLANT_EMAIL, String.class)
                .map(Collections::singleton)
                .orElse(Collections.emptySet());
        log.info("Appellant emails {}", emails);
        return emails;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + REFERENCE_ID_SUFFIX;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap.<String, String>builder()
                .put(SUBJECT_PREFIX_KEY, nonAdaPrefix)
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put(APPEAL_REFERENCE_NUMBER_KEY, asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING))
                .put(APPELLANT_GIVEN_NAMES_KEY, asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING))
                .put(APPELLANT_FAMILY_NAME_KEY, asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING))
                .put(LINK_TO_SERVICE_KEY, iaAipFrontendUrl).build();
    }

}
