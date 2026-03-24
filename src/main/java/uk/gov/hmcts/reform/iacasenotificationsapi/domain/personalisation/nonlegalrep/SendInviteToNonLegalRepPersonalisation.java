package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SHOULD_INVITE_NLR_TO_IDAM;

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
public class SendInviteToNonLegalRepPersonalisation implements EmailNotificationPersonalisation {

    private final String sendInviteToNonLegalRepTemplateId;
    private final String iaAipFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    public SendInviteToNonLegalRepPersonalisation(
        @Value("${govnotify.template.nlr.sendInviteToNonLegalRep.email}") String sendInviteToNonLegalRepTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.sendInviteToNonLegalRepTemplateId = sendInviteToNonLegalRepTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return sendInviteToNonLegalRepTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.NLR_DETAILS, NonLegalRepDetails.class)
            .map(NonLegalRepDetails::getEmailAddress)
            .map(Collections::singleton)
            .orElseThrow(() -> new IllegalStateException("NLR email address is not present"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_SEND_INVITE_TO_NON_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        String createAnAccountSlug = (iaAipFrontendUrl.endsWith("/") ? "" : "/") + "login?register=true";
        asylumCase.write(SHOULD_INVITE_NLR_TO_IDAM, null);
        NonLegalRepDetails nlrDetails = asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class).orElse(null);
        final ImmutableMap.Builder<String, String> fields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("createAnAccountLink", iaAipFrontendUrl + createAnAccountSlug)
            .put("nlrGivenNames", nlrDetails != null ? nlrDetails.getGivenNames() : "Sir /")
            .put("nlrFamilyName", nlrDetails != null ? nlrDetails.getFamilyName() : "Madam")
            .put("Hyperlink to service", iaAipFrontendUrl);

        return fields.build();
    }

}
