package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_PIN_IN_POST;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAipJourney;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantGeneratePinInPostPersonalisationEmail implements EmailNotificationPersonalisation {
    private final String templateId;
    private final RecipientsFinder recipientsFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final String iaAipFrontendUrl;
    private final String iaAipPathToSelfRepresentation;
    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AppellantGeneratePinInPostPersonalisationEmail(
        @Value("${govnotify.template.generatePinInPost.appellant.email}") String templateId,
        @Value("${iaAipPathToSelfRepresentation}") String iaAipPathToSelfRepresentation,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        RecipientsFinder recipientsFinder
    ) {
        this.templateId = templateId;
        this.iaAipPathToSelfRepresentation = iaAipPathToSelfRepresentation;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return isAipJourney(asylumCase) ?
            recipientsFinder.findAll(asylumCase, NotificationType.EMAIL) :
            recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_GENERATED_PIN_IN_POST_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        requireNonNull(asylumCase, "asylumCase must not be null");
        String linkToPiPStartPage = iaAipFrontendUrl + iaAipPathToSelfRepresentation;
        PinInPostDetails pip = asylumCase.read(APPELLANT_PIN_IN_POST, PinInPostDetails.class).orElse(
            PinInPostDetails.builder().accessCode("").expiryDate("").build()
        );
        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("appellantDateOfBirth", defaultDateFormat(asylumCase.read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class).orElse("")))
            .put("ccdCaseId", String.valueOf(callback.getCaseDetails().getId()))
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("linkToPiPStartPage", linkToPiPStartPage)
            .put("securityCode", pip.getAccessCode())
            .put("validDate", defaultDateFormat(pip.getExpiryDate()))
            .put("Hyperlink to service", iaAipFrontendUrl)
            .build();
    }
}
