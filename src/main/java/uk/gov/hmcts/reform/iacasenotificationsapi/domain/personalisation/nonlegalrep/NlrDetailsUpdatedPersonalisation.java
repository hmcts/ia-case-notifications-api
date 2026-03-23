package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class NlrDetailsUpdatedPersonalisation implements EmailNotificationPersonalisation {

    private final String nlrDetailsUpdatedTemplateId;
    private final String iaAipFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    public NlrDetailsUpdatedPersonalisation(
        @Value("${govnotify.template.nlr.nlrDetailsUpdated.email}") String nlrDetailsUpdatedTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.nlrDetailsUpdatedTemplateId = nlrDetailsUpdatedTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return nlrDetailsUpdatedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.NLR_DETAILS, NonLegalRepDetails.class)
            .map(NonLegalRepDetails::getEmailAddress)
            .map(Collections::singleton)
            .orElseThrow(() -> new IllegalStateException("NLR details is not present"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_NON_LEGAL_REP_PHONE_NUMBER_SUBMITTED";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        NonLegalRepDetails nlrDetails = asylumCase.read(AsylumCaseDefinition.NLR_DETAILS, NonLegalRepDetails.class)
            .orElse(null);

        final ImmutableMap.Builder<String, String> fields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("nlrGivenNames", nlrDetails != null ? nlrDetails.getGivenNames() : "Sir /")
            .put("nlrFamilyName", nlrDetails != null ? nlrDetails.getFamilyName() : "Madam")
            .put("Hyperlink to service", iaAipFrontendUrl);

        return fields.build();
    }

}
