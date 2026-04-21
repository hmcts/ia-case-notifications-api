package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionStatus.SUBMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.AWAITING_REASONS_FOR_APPEAL;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.TimeExtensionFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantReviewTimeExtensionRefusedPersonalisationEmailTest {

    private final String smsTemplateId = "someEmailTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppealHomeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String mockedAppellantGivenNames = "someAppellantGivenNames";
    private final String mockedAppellantFamilyName = "someAppellantFamilyName";
    private final String expectedTimeExtensionNewDate = "1 Apr 2020";
    private final String timeExtensionDecisionReason = "the reason";
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    TimeExtensionFinder timeExtensionFinder;
    private IdValue<TimeExtension> mockedTimeExtension;

    private AppellantReviewTimeExtensionRefusedPersonalisationEmail
        appellantReviewTimeExtensionRefusedPersonalisationEmail;

    @BeforeEach
    public void setup() {

        String timeExtensionReason = "the reason";
        String timeExtensionNewDate = "2020-04-01";
        String timeExtensionRequestDate = "2020-03-01";
        mockedTimeExtension = new IdValue<>("someId", new TimeExtension(
            timeExtensionRequestDate,
            timeExtensionReason,
            AWAITING_REASONS_FOR_APPEAL,
            SUBMITTED,
            null,
            TimeExtensionDecision.REFUSED,
            timeExtensionDecisionReason,
            timeExtensionNewDate)
        );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));

        appellantReviewTimeExtensionRefusedPersonalisationEmail =
            new AppellantReviewTimeExtensionRefusedPersonalisationEmail(
                smsTemplateId,
                iaAipFrontendUrl,
                recipientsFinder,
                timeExtensionFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantReviewTimeExtensionRefusedPersonalisationEmail.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REVIEW_TIME_EXTENSION_REFUSED_APPELLANT_AIP_EMAIL", appellantReviewTimeExtensionRefusedPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantReviewTimeExtensionRefusedPersonalisationEmail.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        String mockedAppellantEmailAddress = "appelant@example.net";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(mockedAppellantEmailAddress));

        assertTrue(appellantReviewTimeExtensionRefusedPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(mockedAppellantEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantReviewTimeExtensionRefusedPersonalisationEmail
                .getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        String awaitingReasonsForAppealNextActionText = "why you think the Home Office decision is wrong";

        when(callback.getCaseDetails())
            .thenReturn(new CaseDetails<>(1L, "IA", AWAITING_REASONS_FOR_APPEAL, asylumCase, LocalDateTime.now()));
        when(timeExtensionFinder
            .findCurrentTimeExtension(AWAITING_REASONS_FOR_APPEAL, TimeExtensionStatus.REFUSED, asylumCase))
            .thenReturn(mockedTimeExtension);
        when(timeExtensionFinder.findNextActionText(AWAITING_REASONS_FOR_APPEAL))
            .thenReturn(awaitingReasonsForAppealNextActionText);

        Map<String, String> personalisation =
            appellantReviewTimeExtensionRefusedPersonalisationEmail.getPersonalisation(callback);
        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("HO Ref Number", mockedAppealHomeOfficeReferenceNumber)
            .containsEntry("Given names", mockedAppellantGivenNames)
            .containsEntry("Family name", mockedAppellantFamilyName)
            .containsEntry("decision reason", timeExtensionDecisionReason)
            .containsEntry("Next action text", awaitingReasonsForAppealNextActionText)
            .containsEntry("due date", expectedTimeExtensionNewDate)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        String awaitingReasonsForAppealNextActionText = "why you think the Home Office decision is wrong";

        when(callback.getCaseDetails())
            .thenReturn(new CaseDetails<>(1L, "IA", AWAITING_REASONS_FOR_APPEAL, asylumCase, LocalDateTime.now()));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(timeExtensionFinder
            .findCurrentTimeExtension(AWAITING_REASONS_FOR_APPEAL, TimeExtensionStatus.REFUSED, asylumCase))
            .thenReturn(mockedTimeExtension);
        when(timeExtensionFinder.findNextActionText(AWAITING_REASONS_FOR_APPEAL))
            .thenReturn(awaitingReasonsForAppealNextActionText);

        Map<String, String> personalisation =
            appellantReviewTimeExtensionRefusedPersonalisationEmail.getPersonalisation(callback);
        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("HO Ref Number", "")
            .containsEntry("Given names", "")
            .containsEntry("Family name", "")
            .containsEntry("decision reason", timeExtensionDecisionReason)
            .containsEntry("Next action text", awaitingReasonsForAppealNextActionText)
            .containsEntry("due date", expectedTimeExtensionNewDate)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);
    }
}
