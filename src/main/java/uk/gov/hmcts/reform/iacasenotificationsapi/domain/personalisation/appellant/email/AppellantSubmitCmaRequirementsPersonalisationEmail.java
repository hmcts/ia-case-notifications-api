package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AppellantSubmitCmaRequirementsPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String cmaRequirementsSubmittedAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysToWaitAfterCmaRequirements;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;


    public AppellantSubmitCmaRequirementsPersonalisationEmail(
        @Value("${govnotify.template.submitCmaRequirements.appellant.email}") String cmaRequirementsSubmittedAppellantEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterCmaRequirements}") int daysToWaitAfterCmaRequirements,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.cmaRequirementsSubmittedAppellantEmailTemplateId = cmaRequirementsSubmittedAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterCmaRequirements = daysToWaitAfterCmaRequirements;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getTemplateId() {
        return cmaRequirementsSubmittedAppellantEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_SUBMIT_CMA_REQUIREMENTS_APPELLANT_AIP_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        final String dueDate = systemDateProvider.dueDate(daysToWaitAfterCmaRequirements);

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("HO Ref Number", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Given names", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("Family name", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("due date", dueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
