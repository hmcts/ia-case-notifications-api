package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplicationType.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@Slf4j
@Service
public class DetentionEngagementTeamDecideAnApplicationPersonalisation implements EmailNotificationPersonalisation {

    private static final String DECISION_GRANTED = "Granted";
    private static final String DECISION_REFUSED = "Refused";
    private static final String ADMIN_OFFICER_ROLE = "caseworker-ia-admofficer";

    private final CustomerServicesProvider customerServicesProvider;
    private final String detentionEngagementTeamDecideAnApplicationApplicantTemplateId;
    private final String detentionEngagementTeamDecideAnApplicationOtherPartyTemplateId;
    private final String makeAnApplicationFormLink;
    private final int judgesReviewDeadlineDateDelay;
    private final MakeAnApplicationService makeAnApplicationService;
    private final DateProvider dateProvider;
    private final DetEmailService detEmailService;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public DetentionEngagementTeamDecideAnApplicationPersonalisation(
        @Value("${govnotify.template.decideAnApplication.applicant.detentionEngagementTeam.email}") String detentionEngagementTeamDecideAnApplicationApplicantTemplateId,
        @Value("${govnotify.template.decideAnApplication.otherParty.detentionEngagementTeam.email}") String detentionEngagementTeamDecideAnApplicationOtherPartyTemplateId,
        @Value("${makeAnApplicationFormLink}") String makeAnApplicationFormLink,
        @Value("${judgesReviewDeadlineDateDelay}") int judgesReviewDeadlineDateDelay,
        CustomerServicesProvider customerServicesProvider,
        MakeAnApplicationService makeAnApplicationService,
        DateProvider dateProvider,
        DetEmailService detEmailService
    ) {
        this.detentionEngagementTeamDecideAnApplicationApplicantTemplateId = detentionEngagementTeamDecideAnApplicationApplicantTemplateId;
        this.detentionEngagementTeamDecideAnApplicationOtherPartyTemplateId = detentionEngagementTeamDecideAnApplicationOtherPartyTemplateId;
        this.makeAnApplicationFormLink = makeAnApplicationFormLink;
        this.judgesReviewDeadlineDateDelay = judgesReviewDeadlineDateDelay;
        this.customerServicesProvider = customerServicesProvider;
        this.makeAnApplicationService = makeAnApplicationService;
        this.dateProvider = dateProvider;
        this.detEmailService = detEmailService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        Optional<MakeAnApplication> optionalMakeAnApplication = getMakeAnApplication(asylumCase);
        return isApplicant(optionalMakeAnApplication)
            ? detentionEngagementTeamDecideAnApplicationApplicantTemplateId
            : detentionEngagementTeamDecideAnApplicationOtherPartyTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(detEmailService.getDetEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DECIDE_AN_APPLICATION_DET";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        Optional<MakeAnApplication> optionalMakeAnApplication = getMakeAnApplication(asylumCase);

        String decision = "";
        String applicationType = "";
        String applicationDecisionReason = "No reason given";
        if (optionalMakeAnApplication.isPresent()) {
            MakeAnApplication makeAnApplication = optionalMakeAnApplication.get();
            decision = makeAnApplication.getDecision();
            applicationType = makeAnApplication.getType();
            applicationDecisionReason = makeAnApplication.getDecisionReason();
        }

        boolean applicationGranted = DECISION_GRANTED.equals(decision);
        boolean adjournExpediteTransferOrUpdateHearingReqs = Arrays.asList(
            ADJOURN.toString(),
            EXPEDITE.toString(),
            TRANSFER.toString(),
            UPDATE_HEARING_REQUIREMENTS.toString()
        ).contains(applicationType);

        boolean updateHearingDetailsOrOther = Arrays.asList(
                UPDATE_APPEAL_DETAILS.toString(),
                OTHER.toString()
        ).contains(applicationType);


        String ariaListingReferenceIfPresent = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
            .map(ariaListingReference -> "Listing reference: " + ariaListingReference)
            .orElse("");

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReferenceIfPresent", ariaListingReferenceIfPresent)
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("applicationDecision", transformDecision(optionalMakeAnApplication))
            .put("applicationType", applicationType)
            .put("applicationDecisionReason", applicationDecisionReason)
            .put("granted", applicationGranted ? "yes" : "no")
            .put("grantedAndTimeExtension", applicationGranted && (Objects.equals(TIME_EXTENSION.toString(), applicationType)) ? "yes" : "no")
            .put("grantedAdjournExpediteTransferOrUpdateHearingReqs", applicationGranted && adjournExpediteTransferOrUpdateHearingReqs ? "yes" : "no")
            .put("grantedJudgesReview", applicationGranted && (Objects.equals(JUDGE_REVIEW_LO.toString(), applicationType)) ? "yes" : "no")
            .put("grantedLinkOrUnlik", applicationGranted && (Objects.equals(LINK_OR_UNLINK.toString(), applicationType)) ? "yes" : "no")
            .put("grantedReinstate", applicationGranted && (Objects.equals(REINSTATE.toString(), applicationType)) ? "yes" : "no")
            .put("grantedWithdraw", applicationGranted && (Objects.equals(WITHDRAW.toString(), applicationType)) ? "yes" : "no")
            .put("grantedUpdateAppealDetailsOrOther", applicationGranted && updateHearingDetailsOrOther ? "yes" : "no")
            .put("grantedTransferOutOfAda", applicationGranted && (Objects.equals(TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS.toString(), applicationType)) ? "yes" : "no");


        if (isApplicant(optionalMakeAnApplication)) {
            // If the decision maker is a TCW then change "Tribunal Caseworker" into "Legal Officer"
            String decisionMaker = adaptDecisionMakerName(optionalMakeAnApplication);

            String judgesReviewDeadlineDate = dateProvider.dueDate(judgesReviewDeadlineDateDelay);

            personalizationBuilder = personalizationBuilder
                .put("decisionMaker", decisionMaker)
                .put("judgesReviewDeadlineDate", judgesReviewDeadlineDate)
                .put("makeAnApplicationLink", makeAnApplicationFormLink);
        }

        return personalizationBuilder.build();
    }

    private String adaptDecisionMakerName(Optional<MakeAnApplication> optionalMakeAnApplication) {
        return optionalMakeAnApplication
            .map(makeAnApplication -> Objects.equals(makeAnApplication.getDecisionMaker(), "Tribunal Caseworker")
                ? "Legal Officer"
                : makeAnApplication.getDecisionMaker()
            )
            .orElse("");
    }

    private String transformDecision(Optional<MakeAnApplication> makeAnApplication) {
        return makeAnApplication.map(app -> {
            String decision = app.getDecision();
            if (DECISION_GRANTED.equals(decision)) {
                decision =  isApplicant(makeAnApplication) ? "grant" : "granted";
            } else if (DECISION_REFUSED.equals(decision)) {
                decision = isApplicant(makeAnApplication) ? "refuse" : "refused";
            } else {
                decision = "";
            }
            return decision;
        }).orElse("");
    }

    private Optional<MakeAnApplication> getMakeAnApplication(AsylumCase asylumCase) {
        return makeAnApplicationService.getMakeAnApplication(asylumCase, true);
    }

    private boolean isApplicant(Optional<MakeAnApplication> makeAnApplication) {
        return makeAnApplication.map(app -> Objects.equals(ADMIN_OFFICER_ROLE, app.getApplicantRole())
        ).orElse(false);
    }
}

