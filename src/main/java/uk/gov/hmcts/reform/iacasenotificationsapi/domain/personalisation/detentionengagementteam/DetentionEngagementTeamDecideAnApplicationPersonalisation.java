package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplicationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@Slf4j
@Service
public class DetentionEngagementTeamDecideAnApplicationPersonalisation implements EmailNotificationPersonalisation {

    private static final String DECISION_GRANTED = "Granted";
    private static final String DECISION_REFUSED = "Refused";

    private final CustomerServicesProvider customerServicesProvider;
    private final String detentionEngagementTeamDecideAnApplicationTemplateId;
    private final String makeAnApplicationFormLink;
    private final int judgesReviewDeadlineDateDelay;
    private final String detentionEngagementTeamEmail;
    private final MakeAnApplicationService makeAnApplicationService;
    private final DateProvider dateProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public DetentionEngagementTeamDecideAnApplicationPersonalisation(
        @Value("${govnotify.template.decideAnApplication.det.email}") String detentionEngagementTeamDecideAnApplicationTemplateId,
        @Value("${detentionEngagementTeamEmailAddress}") String detentionEngagementTeamEmail,
        @Value("${makeAnApplicationFormLink}") String makeAnApplicationFormLink,
        @Value("${judgesReviewDeadlineDateDelay}") int judgesReviewDeadlineDateDelay,
        CustomerServicesProvider customerServicesProvider,
        MakeAnApplicationService makeAnApplicationService,
        DateProvider dateProvider
    ) {
        this.detentionEngagementTeamDecideAnApplicationTemplateId = detentionEngagementTeamDecideAnApplicationTemplateId;
        this.detentionEngagementTeamEmail = detentionEngagementTeamEmail;
        this.makeAnApplicationFormLink = makeAnApplicationFormLink;
        this.judgesReviewDeadlineDateDelay = judgesReviewDeadlineDateDelay;
        this.customerServicesProvider = customerServicesProvider;
        this.makeAnApplicationService = makeAnApplicationService;
        this.dateProvider = dateProvider;
    }

    @Override
    public String getTemplateId() {
        return detentionEngagementTeamDecideAnApplicationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(detentionEngagementTeamEmail);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DECIDE_AN_APPLICATION_DET";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        Optional<MakeAnApplication> optionalMakeAnApplication = getMakeAnApplication(asylumCase);

        // Turn the decision term into the verb to be used in the notification
        String applicationDecision = getApplicationDecisionVerb(optionalMakeAnApplication);

        String actionToTake = getActionToTakeBasedOnDecision(optionalMakeAnApplication);

        // If the decision maker is a TCW then change "Tribunal Caseworker" into "Legal Officer"
        String decisionMaker = adaptDecisionMakerName(optionalMakeAnApplication);

        String judgesReviewDeadlineDate = dateProvider.dueDate(judgesReviewDeadlineDateDelay);

        String ariaListingReferenceIfPresent = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
            .map(ariaListingReference -> "\nListing reference: " + ariaListingReference)
            .orElse("");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReferenceIfPresent", ariaListingReferenceIfPresent)
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("decisionMaker", decisionMaker)
            .put("applicationDecision", applicationDecision)
            .put("applicationType", optionalMakeAnApplication.map(MakeAnApplication::getType).orElse(""))
            .put("applicationDecisionReason", getMakeAnApplication(asylumCase).map(MakeAnApplication::getDecisionReason).orElse("No reason given"))
            .put("action", actionToTake)
            .put("judgesReviewDeadlineDate", judgesReviewDeadlineDate)
            .put("makeAnApplicationLink", makeAnApplicationFormLink)
            .build();
    }

    private String adaptDecisionMakerName(Optional<MakeAnApplication> optionalMakeAnApplication) {
        return optionalMakeAnApplication
            .map(makeAnApplication -> Objects.equals(makeAnApplication.getDecisionMaker(), "Tribunal Caseworker")
                ? "Legal Officer"
                : makeAnApplication.getDecisionMaker()
            )
            .orElse("");
    }

    private String getActionToTakeBasedOnDecision(Optional<MakeAnApplication> optionalMakeAnApplication) {
        return optionalMakeAnApplication
            .map(makeAnApplication -> {
                String type = makeAnApplication.getType();
                MakeAnApplicationType makeAnApplicationType = MakeAnApplicationType.from(type)
                    .orElseThrow(() -> new IllegalStateException("Unrecognized makeAnApplicationType"));
                return actionToTakeAfterGrant(makeAnApplicationType);
            })
            .orElse("");
    }

    private String getApplicationDecisionVerb(Optional<MakeAnApplication> optionalMakeAnApplication) {
        return optionalMakeAnApplication
            .map(makeAnApplication -> Objects.equals(makeAnApplication.getDecision(), DECISION_GRANTED)
                ? "grant"
                : Objects.equals(makeAnApplication.getDecision(), DECISION_REFUSED)
                ? "refuse"
                : "")
            .orElse("");
    }

    private Optional<MakeAnApplication> getMakeAnApplication(AsylumCase asylumCase) {
        return makeAnApplicationService.getMakeAnApplication(asylumCase, true);
    }

    private String actionToTakeAfterGrant(MakeAnApplicationType makeAnApplicationTypes) {
        String action = "";
        switch (makeAnApplicationTypes) {
            case TIME_EXTENSION:
                action = "The Tribunal will give you more time to complete your next task. You will get a notification with the new date soon.";
                break;
            case ADJOURN:
            case EXPEDITE:
            case TRANSFER:
                action = "The details of your hearing will be updated. The Tribunal will contact you when this happens.";
                break;
            case JUDGE_REVIEW:
                action = "The decision on your original request will be overturned. The Tribunal will contact you if there is something you need to do next.";
                break;
            case LINK_OR_UNLINK:
                action = "This appeal will be linked or unlinked. The Tribunal will contact you when this happens.";
                break;
            case REINSTATE:
                action = "This appeal will be reinstated and will continue from the point where it was ended. The Tribunal will contact you when this happens.";
                break;
            case WITHDRAW:
                action = "The Tribunal will end the appeal. The Tribunal will contact you when this happens.";
                break;
            case OTHER:
                action = "The Tribunal will contact you when it makes the changes you requested.";
                break;
            default:
                break;
        }
        return action;
    }



}

