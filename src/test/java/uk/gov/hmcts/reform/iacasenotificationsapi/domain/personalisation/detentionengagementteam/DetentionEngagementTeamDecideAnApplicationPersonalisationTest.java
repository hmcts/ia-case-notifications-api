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

    private final Long caseId = 12345L;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "1234-1234-1234-1234";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String decisionReason = "someDecisionReason";
    private final String detentionEngagementTeamDecideAnApplicationTemplateId = "detentionEngagementTeamDecideAnApplicationTemplateId";
    private final String makeAnApplicationFormLink = "someLink";
    private final int judgesReviewDeadlineDateDelay = 14;
    private final String detentionEngagementTeamEmail = "det@email.com";
    private final String customerServiceTelephone = "0123456789";
    private final String customerServiceEmail = "hm@email.com";
    private final String calculatedDeadline = "03 March 2023";

    private DetentionEngagementTeamDecideAnApplicationPersonalisation detentionEngagementTeamDecideAnApplicationPersonalisation;

    @BeforeEach
    void setup() {
        detentionEngagementTeamDecideAnApplicationPersonalisation = new DetentionEngagementTeamDecideAnApplicationPersonalisation(
            detentionEngagementTeamDecideAnApplicationTemplateId,
            detentionEngagementTeamEmail,
            makeAnApplicationFormLink,
            judgesReviewDeadlineDateDelay,
            customerServicesProvider,
            makeAnApplicationService,
            dateProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(detentionEngagementTeamDecideAnApplicationTemplateId, detentionEngagementTeamDecideAnApplicationPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_DECIDE_AN_APPLICATION_DET",
            detentionEngagementTeamDecideAnApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_det_email_address() {
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
    void should_return_personalisation_of_all_information_given(MakeAnApplicationType makeAnApplicationType) {
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

        String expectedAction = personalisation.get("action");

        switch (makeAnApplicationType) {
            case TIME_EXTENSION:
                assertEquals("The Tribunal will give you more time to complete your next task. "
                             + "You will get a notification with the new date soon.", expectedAction);
                break;
            case ADJOURN:
            case EXPEDITE:
            case TRANSFER:
                assertEquals( "The details of your hearing will be updated. The Tribunal "
                              + "will contact you when this happens.", expectedAction);
                break;
            case JUDGE_REVIEW:
                assertEquals("The decision on your original request will be overturned. "
                             + "The Tribunal will contact you if there is something you need to do next.",
                    expectedAction);
                break;
            case LINK_OR_UNLINK:
                assertEquals("This appeal will be linked or unlinked. The Tribunal will contact you "
                             + "when this happens.", expectedAction);
                break;
            case REINSTATE:
                assertEquals("This appeal will be reinstated and will continue from the point "
                             + "where it was ended. The Tribunal will contact you when this happens.", expectedAction);
                break;
            case WITHDRAW:
                assertEquals("The Tribunal will end the appeal. The Tribunal will contact you "
                             + "when this happens.", expectedAction);
                break;
            case OTHER:
                assertEquals("The Tribunal will contact you when it makes the changes you "
                             + "requested.", expectedAction);
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

