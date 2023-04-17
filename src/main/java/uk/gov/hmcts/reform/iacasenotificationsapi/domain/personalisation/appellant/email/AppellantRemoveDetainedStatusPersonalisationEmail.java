package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
@Service
public class AppellantRemoveDetainedStatusPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantRemoveDetainedStatusPersonalisationEmailTemplateId;
    private final RecipientsFinder recipientsFinder;

    public AppellantRemoveDetainedStatusPersonalisationEmail(
            @NotNull(message = "appellantRemoveDetentionStatusPersonalisationEmailTemplateId cannot be null")
            @Value("${govnotify.template.removeDetainedStatus.appellant.email}") String appellantRemoveDetentionStatusPersonalisationEmailTemplateId,
            RecipientsFinder recipientsFinder) {
        this.appellantRemoveDetainedStatusPersonalisationEmailTemplateId = appellantRemoveDetentionStatusPersonalisationEmailTemplateId;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appellantRemoveDetainedStatusPersonalisationEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_DETENTION_STATUS_AIP_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        String ariaListingReference = asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(null);
        return ImmutableMap
                .<String, String>builder()
                .put("appealListed", ariaListingReference != null ? "Yes" : "No")
                .put("ariaListingReference", ariaListingReference)
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .build();
    }
}
