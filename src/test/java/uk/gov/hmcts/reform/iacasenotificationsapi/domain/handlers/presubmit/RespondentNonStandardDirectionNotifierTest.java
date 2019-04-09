package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RespondentNonStandardDirectionNotifierTest {

    private static final String RESPONDENT_NON_STANDARD_DIRECTION_TEMPLATE = "template-id";

    @Mock private DirectionFinder directionFinder;
    @Mock private NotificationSender notificationSender;
    @Mock private StringProvider stringProvider;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Direction nonStandardDirection;

    @Captor private ArgumentCaptor<List<IdValue<String>>> existingNotificationsSentCaptor;

    final long caseId = 123L;

    final String respondentEmailAddress = "respondent@example.com";
    final HearingCentre hearingCentre = HearingCentre.MANCHESTER;
    final String hearingCentreForDisplay = "Manchester";
    final String homeOfficeReferenceNumber = "SOMETHING";
    final String appealReferenceNumber = "PA/001/2018";
    final String appellantGivenNames = "Jane";
    final String appellantFamilyName = "Doe";
    final String directionExplanation = "Do the thing";
    final String directionDateDue = "2019-12-31";
    final String directionDateDueFormatted = "31 Dec 2019";

    final Map<String, String> expectedPersonalisation =
        ImmutableMap
            .<String, String>builder()
            .put("HearingCentre", hearingCentreForDisplay)
            .put("Appeal Ref Number", appealReferenceNumber)
            .put("HORef", homeOfficeReferenceNumber)
            .put("Given names", appellantGivenNames)
            .put("Family name", appellantFamilyName)
            .put("Explanation", directionExplanation)
            .put("due date", directionDateDueFormatted)
            .build();

    final String expectedNotificationId = "ABC-DEF-GHI-JKL";
    final String expectedNotificationReference = caseId + "_RESPONDENT_NON_STANDARD_DIRECTION";

    private RespondentNonStandardDirectionNotifier nespondentNonStandardDirectionNotifier;

    @Before
    public void setUp() {
        nespondentNonStandardDirectionNotifier =
            new RespondentNonStandardDirectionNotifier(
                RESPONDENT_NON_STANDARD_DIRECTION_TEMPLATE,
                respondentEmailAddress,
                directionFinder,
                notificationSender,
                stringProvider
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getNotificationsSent()).thenReturn(Optional.empty());

        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(nonStandardDirection));
        when(stringProvider.get("hearingCentre", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreForDisplay));

        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.getHearingCentre()).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.getHomeOfficeReferenceNumber()).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.of(appellantFamilyName));
        when(nonStandardDirection.getExplanation()).thenReturn(directionExplanation);
        when(nonStandardDirection.getParties()).thenReturn(Parties.RESPONDENT);
        when(nonStandardDirection.getDateDue()).thenReturn(directionDateDue);

        when(notificationSender.sendEmail(
            RESPONDENT_NON_STANDARD_DIRECTION_TEMPLATE,
            respondentEmailAddress,
            expectedPersonalisation,
            expectedNotificationReference
        )).thenReturn(expectedNotificationId);
    }

    @Test
    public void should_send_respondent_evidence_direction_notification() {

        final List<IdValue<String>> existingNotifications =
            new ArrayList<>(Arrays.asList(
                new IdValue<>("some-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ")
            ));

        when(asylumCase.getNotificationsSent()).thenReturn(Optional.of(existingNotifications));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            RESPONDENT_NON_STANDARD_DIRECTION_TEMPLATE,
            respondentEmailAddress,
            expectedPersonalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(existingNotificationsSentCaptor.capture());

        List<IdValue<String>> actualExistingNotificationsSent =
            existingNotificationsSentCaptor
                .getAllValues()
                .get(0);

        assertEquals(2, actualExistingNotificationsSent.size());

        assertEquals("some-notification-sent", actualExistingNotificationsSent.get(0).getId());
        assertEquals("ZZZ-ZZZ-ZZZ-ZZZ", actualExistingNotificationsSent.get(0).getValue());

        assertEquals(caseId + "_RESPONDENT_NON_STANDARD_DIRECTION", actualExistingNotificationsSent.get(1).getId());
        assertEquals(expectedNotificationId, actualExistingNotificationsSent.get(1).getValue());
    }

    @Test
    public void should_send_respondent_evidence_direction_notification_when_no_notifications_exist() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            RESPONDENT_NON_STANDARD_DIRECTION_TEMPLATE,
            respondentEmailAddress,
            expectedPersonalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(existingNotificationsSentCaptor.capture());

        List<IdValue<String>> actualExistingNotificationsSent =
            existingNotificationsSentCaptor
                .getAllValues()
                .get(0);

        assertEquals(1, actualExistingNotificationsSent.size());

        assertEquals(caseId + "_RESPONDENT_NON_STANDARD_DIRECTION", actualExistingNotificationsSent.get(0).getId());
        assertEquals(expectedNotificationId, actualExistingNotificationsSent.get(0).getValue());
    }

    @Test
    public void should_send_respondent_evidence_direction_notification_using_defaults_where_available() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("HearingCentre", hearingCentreForDisplay)
                .put("Appeal Ref Number", "")
                .put("HORef", "")
                .put("Given names", "")
                .put("Family name", "")
                .put("Explanation", directionExplanation)
                .put("due date", directionDateDueFormatted)
                .build();

        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.getHearingCentre()).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getHomeOfficeReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.empty());
        when(nonStandardDirection.getExplanation()).thenReturn(directionExplanation);
        when(nonStandardDirection.getParties()).thenReturn(Parties.RESPONDENT);
        when(nonStandardDirection.getDateDue()).thenReturn(directionDateDue);

        when(notificationSender.sendEmail(
            RESPONDENT_NON_STANDARD_DIRECTION_TEMPLATE,
            respondentEmailAddress,
            expectedPersonalisation,
            expectedNotificationReference
        )).thenReturn(expectedNotificationId);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender, times(1)).sendEmail(
            RESPONDENT_NON_STANDARD_DIRECTION_TEMPLATE,
            respondentEmailAddress,
            expectedPersonalisation,
            expectedNotificationReference
        );

        verify(asylumCase, times(1)).setNotificationsSent(existingNotificationsSentCaptor.capture());

        List<IdValue<String>> actualExistingNotificationsSent =
            existingNotificationsSentCaptor
                .getAllValues()
                .get(0);

        assertEquals(1, actualExistingNotificationsSent.size());

        assertEquals(caseId + "_RESPONDENT_NON_STANDARD_DIRECTION", actualExistingNotificationsSent.get(0).getId());
        assertEquals(expectedNotificationId, actualExistingNotificationsSent.get(0).getValue());
    }

    @Test
    public void should_not_notify_if_parties_is_not_exclusively_respondent() {

        Arrays.asList(
            Parties.LEGAL_REPRESENTATIVE,
            Parties.BOTH
        ).forEach(parties -> {

            when(nonStandardDirection.getParties()).thenReturn(parties);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(asylumCase, callbackResponse.getData());

            verifyZeroInteractions(notificationSender);

            reset(nonStandardDirection);
        });
    }

    @Test
    public void should_throw_when_non_standard_direction_not_present() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("non-standard direction is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_hearing_centre_not_present() {

        when(asylumCase.getHearingCentre()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("hearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_hearing_centre_display_string_not_present() {

        when(stringProvider.get("hearingCentre", hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("hearingCentre display string is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        assertThatThrownBy(() -> nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        assertThatThrownBy(() -> nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        when(caseDetails.getState()).thenReturn(State.HEARING_AND_OUTCOME);
        assertThatThrownBy(() -> nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        final List<State> allowedCaseStates =
            Arrays.asList(
                State.APPEAL_SUBMITTED,
                State.APPEAL_SUBMITTED_OUT_OF_TIME,
                State.AWAITING_RESPONDENT_EVIDENCE,
                State.CASE_BUILDING,
                State.CASE_UNDER_REVIEW,
                State.RESPONDENT_REVIEW,
                State.SUBMIT_HEARING_REQUIREMENTS,
                State.LISTING
            );

        for (State state : State.values()) {

            for (Event event : Event.values()) {

                when(callback.getEvent()).thenReturn(event);
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getState()).thenReturn(state);

                for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                    boolean canHandle = nespondentNonStandardDirectionNotifier.canHandle(callbackStage, callback);

                    if (event == Event.SEND_DIRECTION
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && allowedCaseStates.contains(state)) {

                        assertTrue(canHandle);
                    } else {
                        assertFalse(canHandle);
                    }
                }

                reset(callback, caseDetails);
            }
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> nespondentNonStandardDirectionNotifier.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> nespondentNonStandardDirectionNotifier.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> nespondentNonStandardDirectionNotifier.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> nespondentNonStandardDirectionNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
