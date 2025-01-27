package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;


@Service
public class AppellantRespondentReviewPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String requestRespondentReviewAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final DirectionFinder directionFinder;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AppellantRespondentReviewPersonalisationEmail(
            @Value("${govnotify.template.reviewDirection.appellant.email}") String requestRespondentReviewAppellantEmailTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            DirectionFinder directionFinder, CustomerServicesProvider customerServicesProvider) {

        this.requestRespondentReviewAppellantEmailTemplateId = requestRespondentReviewAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.directionFinder = directionFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return requestRespondentReviewAppellantEmailTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REQUEST_RESPONDENT_REVIEW_APPELLANT_AIP_EMAIL";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
                directionFinder
                        .findFirst(asylumCase, DirectionTag.RESPONDENT_REVIEW)
                        .orElseThrow(() -> new IllegalStateException("Appellant respondent review is not present"));


        final String directionDueDate =
                LocalDate
                        .parse(direction.getDateDue())
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("dueDate", directionDueDate)
            .put("HearingCentre", getHearingCentreName(asylumCase))
                .build();
    }

    private String getHearingCentreName(AsylumCase caseData) {

        HearingCentre hearingCentre = caseData.read(AsylumCaseDefinition.HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));
        String hearingCentreName = String.valueOf(hearingCentre);

        String[] words = hearingCentreName.replaceAll("([a-z])([A-Z])", "$1 $2").split(" ");
        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            formattedName.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }

        return formattedName.toString().trim();
    }
}
