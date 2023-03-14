package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplicationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamDecideAnApplicationPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private MakeAnApplicationService makeAnApplicationService;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private MakeAnApplication makeAnApplication;
    @Mock
    private DetEmailService detEmailService;

    private final Long caseId = 12345L;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String decisionReason = "someDecisionReason";
    private final String detentionEngagementTeamDecideAnApplicationApplicantTemplateId = "detentionEngagementTeamDecideAnApplicationApplicantTemplateId";
    private final String detentionEngagementTeamDecideAnApplicationOtherPartyTemplateId = "detentionEngagementTeamDecideAnApplicationOtherPartyTemplateId";
    private final String makeAnApplicationFormLink = "someLink";
    private final int judgesReviewDeadlineDateDelay = 14;
    private final String customerServiceTelephone = "0123456789";
    private final String customerServiceEmail = "hm@email.com";
    private final String calculatedDeadline = "03 March 2023";
    private final String adminOfficerRole = "caseworker-ia-admofficer";

    private DetentionEngagementTeamDecideAnApplicationPersonalisation detentionEngagementTeamDecideAnApplicationPersonalisation;

    @BeforeEach
    void setup() {
        detentionEngagementTeamDecideAnApplicationPersonalisation = new DetentionEngagementTeamDecideAnApplicationPersonalisation(
            detentionEngagementTeamDecideAnApplicationApplicantTemplateId,
            detentionEngagementTeamDecideAnApplicationOtherPartyTemplateId,
            makeAnApplicationFormLink,
            judgesReviewDeadlineDateDelay,
            customerServicesProvider,
            makeAnApplicationService,
            dateProvider,
            detEmailService
        );
    }

    @Test
    void should_return_given_template_id() {

        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(makeAnApplication));

        assertEquals(detentionEngagementTeamDecideAnApplicationOtherPartyTemplateId, detentionEngagementTeamDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplication.getApplicantRole()).thenReturn(adminOfficerRole);
        assertEquals(detentionEngagementTeamDecideAnApplicationApplicantTemplateId, detentionEngagementTeamDecideAnApplicationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_DECIDE_AN_APPLICATION_DET",
            detentionEngagementTeamDecideAnApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
        String detentionEngagementTeamEmail = "det@email.com";
        when(detEmailService.getAdaDetEmailAddress()).thenReturn(detentionEngagementTeamEmail);

        assertTrue(
            detentionEngagementTeamDecideAnApplicationPersonalisation.getRecipientsList(asylumCase).contains(detentionEngagementTeamEmail));
    }

    @ParameterizedTest
    @EnumSource(value = MakeAnApplicationType.class, names = {
        "ADJOURN",
        "EXPEDITE",
        "JUDGE_REVIEW",
        "JUDGE_REVIEW_LO",
        "LINK_OR_UNLINK",
        "TIME_EXTENSION",
        "TRANSFER",
        "WITHDRAW",
        "UPDATE_HEARING_REQUIREMENTS",
        "UPDATE_APPEAL_DETAILS",
        "REINSTATE",
        "TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS",
        "OTHER"
    })
    void should_return_personalisation_of_all_information_given_as_applicant(MakeAnApplicationType makeAnApplicationType) {
        initializePrefixes(detentionEngagementTeamDecideAnApplicationPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(dateProvider.dueDate(judgesReviewDeadlineDateDelay)).thenReturn(calculatedDeadline);

        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(makeAnApplication));
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getType()).thenReturn(makeAnApplicationType.getValue());
        when(makeAnApplication.getDecisionMaker()).thenReturn("Judge");
        when(makeAnApplication.getDecisionReason()).thenReturn(decisionReason);
        when(makeAnApplication.getApplicantRole()).thenReturn(adminOfficerRole);
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServiceTelephone)
            .put("customerServicesEmail", customerServiceEmail)
            .build());

        Map<String, String> personalisation = detentionEngagementTeamDecideAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals(customerServiceTelephone, personalisation.get("customerServicesTelephone"));
        assertEquals(customerServiceEmail, personalisation.get("customerServicesEmail"));
        assertEquals("Accelerated detained appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReferenceIfPresent"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals("Judge", personalisation.get("decisionMaker"));
        assertEquals("grant", personalisation.get("applicationDecision"));
        assertEquals(makeAnApplicationType.getValue(), personalisation.get("applicationType"));
        assertEquals(decisionReason, personalisation.get("applicationDecisionReason"));
        assertEquals(calculatedDeadline, personalisation.get("judgesReviewDeadlineDate"));
        assertEquals(makeAnApplicationFormLink, personalisation.get("makeAnApplicationLink"));

        switch (makeAnApplicationType) {
            case TIME_EXTENSION:
                assertEquals("yes", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case ADJOURN:
            case EXPEDITE:
            case TRANSFER:
                assertEquals("yes", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case JUDGE_REVIEW:
                assertEquals("yes", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case LINK_OR_UNLINK:
                assertEquals("yes", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case REINSTATE:
                assertEquals("yes", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case WITHDRAW:
                assertEquals("yes", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case OTHER:
                assertEquals("yes", personalisation.get("grantedOther"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                break;
            default:
                break;
        }
    }

    @ParameterizedTest
    @EnumSource(value = MakeAnApplicationType.class, names = {
        "ADJOURN",
        "EXPEDITE",
        "JUDGE_REVIEW",
        "JUDGE_REVIEW_LO",
        "LINK_OR_UNLINK",
        "TIME_EXTENSION",
        "TRANSFER",
        "WITHDRAW",
        "UPDATE_HEARING_REQUIREMENTS",
        "UPDATE_APPEAL_DETAILS",
        "REINSTATE",
        "TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS",
        "OTHER"
    })
    void should_return_personalisation_of_all_information_given_as_other_party(MakeAnApplicationType makeAnApplicationType) {
        initializePrefixes(detentionEngagementTeamDecideAnApplicationPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(makeAnApplication));
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getType()).thenReturn(makeAnApplicationType.getValue());
        when(makeAnApplication.getDecisionReason()).thenReturn(decisionReason);
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServiceTelephone)
            .put("customerServicesEmail", customerServiceEmail)
            .build());

        Map<String, String> personalisation = detentionEngagementTeamDecideAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals(customerServiceTelephone, personalisation.get("customerServicesTelephone"));
        assertEquals(customerServiceEmail, personalisation.get("customerServicesEmail"));
        assertEquals("Accelerated detained appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReferenceIfPresent"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals("granted", personalisation.get("applicationDecision"));
        assertEquals(makeAnApplicationType.getValue(), personalisation.get("applicationType"));
        assertEquals(decisionReason, personalisation.get("applicationDecisionReason"));
        assertEquals(null, personalisation.get("decisionMaker"));
        assertEquals(null, personalisation.get("judgesReviewDeadlineDate"));
        assertEquals(null, personalisation.get("makeAnApplicationLink"));

        switch (makeAnApplicationType) {
            case TIME_EXTENSION:
                assertEquals("yes", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case ADJOURN:
            case EXPEDITE:
            case TRANSFER:
                assertEquals("yes", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case JUDGE_REVIEW:
                assertEquals("yes", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case LINK_OR_UNLINK:
                assertEquals("yes", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case REINSTATE:
                assertEquals("yes", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case WITHDRAW:
                assertEquals("yes", personalisation.get("grantedWithdraw"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedOther"));
                break;
            case OTHER:
                assertEquals("yes", personalisation.get("grantedOther"));
                assertEquals("no", personalisation.get("grantedAndTimeExtension"));
                assertEquals("no", personalisation.get("grantedAdjournExpediteOrTransfer"));
                assertEquals("no", personalisation.get("grantedJudgesReview"));
                assertEquals("no", personalisation.get("grantedLinkOrUnlik"));
                assertEquals("no", personalisation.get("grantedReinstate"));
                assertEquals("no", personalisation.get("grantedWithdraw"));
                break;
            default:
                break;
        }
    }

    @Test
    void should_return_personalisation_with_formatted_tribunal_caseworker_term() {
        initializePrefixes(detentionEngagementTeamDecideAnApplicationPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));

        when(dateProvider.dueDate(judgesReviewDeadlineDateDelay)).thenReturn(calculatedDeadline);

        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(makeAnApplication));
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        when(makeAnApplication.getType()).thenReturn("Expedite");
        when(makeAnApplication.getDecisionMaker()).thenReturn("Tribunal Caseworker");
        when(makeAnApplication.getDecisionReason()).thenReturn(decisionReason);
        when(makeAnApplication.getApplicantRole()).thenReturn(adminOfficerRole);
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", customerServiceTelephone)
            .put("customerServicesEmail", customerServiceEmail)
            .build());

        Map<String, String> personalisation = detentionEngagementTeamDecideAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals("refuse", personalisation.get("applicationDecision"));
        assertEquals("Legal Officer", personalisation.get("decisionMaker"));
    }
}

