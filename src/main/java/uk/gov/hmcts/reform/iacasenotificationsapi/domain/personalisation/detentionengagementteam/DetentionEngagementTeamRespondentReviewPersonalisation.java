package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

@Service
public class DetentionEngagementTeamRespondentReviewPersonalisation implements EmailNotificationPersonalisation {

    private final String detentionEngagementTeamRespondentReviewTemplateId;
    private final RecipientsFinder recipientsFinder;

    public DetentionEngagementTeamRespondentReviewPersonalisation(
        @NotNull(message = "DetentionEngagementTeamRespondentReviewTemplateId cannot be null")
        @Value("${govnotify.template.reviewDirection.detentionTeam.email}") String detentionEngagementTeamRespondentReviewTemplateId,
        RecipientsFinder recipientsFinder) {

        this.detentionEngagementTeamRespondentReviewTemplateId = detentionEngagementTeamRespondentReviewTemplateId;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return detentionEngagementTeamRespondentReviewTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DENTENTION_ENGAGEMENT_TEAM_REQUEST_RESPONDENT_REVIEW";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return Collections.singleton("somedummyemail@gmail.com");
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("directionDueDate", asylumCase.read(AsylumCaseDefinition.SEND_DIRECTION_DATE_DUE, String.class).orElse(""))
            .build();
    }
}
