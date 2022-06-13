package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import javax.validation.constraints.NotNull;

import java.util.Collection;
import java.util.Collections;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

public class AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail {

    private final String templateId;

    public AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail(
        @NotNull(message = "forceCaseProgressionToCaseUnderReviewAiPTemplateId cannot be null") @Value("${govnotify.template.forceCaseProgression.caseBuilding.to.caseUnderReview.appellant.email}") String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getReferenceId(Long caseId) {
        return caseId + "_FORCE_CASE_TO_CASE_UNDER_REVIEW_AIP";
    }

    public Collection<String> getRecipientList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(APPELLANT_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("appellantEmailAddress is not present")));
    }
}


