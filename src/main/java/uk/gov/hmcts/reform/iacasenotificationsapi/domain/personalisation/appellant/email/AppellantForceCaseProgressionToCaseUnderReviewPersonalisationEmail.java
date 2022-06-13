package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import java.util.Collections;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

public class AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String templateId;

    public AppellantForceCaseProgressionToCaseUnderReviewPersonalisationEmail(
        @NotNull(message = "forceCaseProgressionToCaseUnderReviewAiPTemplateId cannot be null") @Value("${govnotify.template.forceCaseProgression.caseBuilding.to.caseUnderReview.appellant.email}") String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(AsylumCaseDefinition.APPELLANT_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("appellantEmailAddress is not present")));
    }

    public String getReferenceId(Long caseId) {
        return caseId + "_FORCE_CASE_TO_CASE_UNDER_REVIEW_AIP";
    }

}


