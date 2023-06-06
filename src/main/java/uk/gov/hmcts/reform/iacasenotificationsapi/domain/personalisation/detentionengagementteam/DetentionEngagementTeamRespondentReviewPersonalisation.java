package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@Service
public class DetentionEngagementTeamRespondentReviewPersonalisation implements EmailNotificationPersonalisation {

    private final String detentionEngagementTeamRespondentReviewTemplateId;
    private final DirectionFinder directionFinder;
    private final DetEmailService detEmailService;

    public DetentionEngagementTeamRespondentReviewPersonalisation(
        @NotNull(message = "DetentionEngagementTeamRespondentReviewTemplateId cannot be null")
        @Value("${govnotify.template.reviewDirection.detentionTeam.email}") String detentionEngagementTeamRespondentReviewTemplateId,
        DirectionFinder directionFinder,
        DetEmailService detEmailService
    ) {
        this.detentionEngagementTeamRespondentReviewTemplateId = detentionEngagementTeamRespondentReviewTemplateId;
        this.directionFinder = directionFinder;
        this.detEmailService = detEmailService;
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
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);
        return !detentionFacility.get().equals("immigrationRemovalCentre")
            ? Collections.emptySet() : Collections.singleton(detEmailService.getDetEmailAddress(asylumCase));
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
                directionFinder
                        .findFirst(asylumCase, DirectionTag.RESPONDENT_REVIEW)
                        .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.RESPONDENT_REVIEW + "' is not present"));

        final String directionDueDate =
                LocalDate
                        .parse(direction.getDateDue())
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("directionDueDate", directionDueDate)
            .build();
    }
}
