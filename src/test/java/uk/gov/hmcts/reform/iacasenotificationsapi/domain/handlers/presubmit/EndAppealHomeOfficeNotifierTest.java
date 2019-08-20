package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EndAppealPersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class EndAppealHomeOfficeNotifierTest extends BaseNotifierTest {

    private static final String END_APPEAL_HOME_OFFICE_TEMPLATE = "template-id";

    @Mock private EndAppealPersonalisationFactory endAppealPersonalisationFactory;

    @Mock private GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;

    private final String expectedNotificationReference = caseId + "_END_APPEAL_HOME_OFFICE";

    private EndAppealNotifier endAppealNotifier;

    @Before
    public void setUp() {

        baseNotifierTestSetUp(Event.END_APPEAL);

        when(govNotifyTemplateIdConfiguration.getEndAppealHomeOfficeTemplateId()).thenReturn(END_APPEAL_HOME_OFFICE_TEMPLATE);

        endAppealNotifier =
            new EndAppealNotifier(
                govNotifyTemplateIdConfiguration,
                HOME_OFFICE_EMAIL_ADDRESS,
                notificationSender,
                notificationIdAppender,
                endAppealPersonalisationFactory
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.END_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(LEGAL_REP_EMAIL_ADDRESS));
        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.empty());
        when(endAppealPersonalisationFactory.create(asylumCase)).thenReturn(personalisation);

        when(notificationSender.sendEmail(
            END_APPEAL_HOME_OFFICE_TEMPLATE,
            HOME_OFFICE_EMAIL_ADDRESS,
            personalisation,
                expectedNotificationReference
        )).thenReturn(notificationId);
    }

    @Test
    public void should_send_end_appeal_email_notification_to_hearing_centre() {

        final List<IdValue<String>> existingNotifications =
            new ArrayList<>(Collections.singletonList(
                new IdValue<>("case-listed-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ")
            ));

        final List<IdValue<String>> expectedNotifications =
            new ArrayList<>(Arrays.asList(
                new IdValue<>("case-listed-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ"),
                new IdValue<>(expectedNotificationReference, notificationId)
            ));

        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(existingNotifications));

        when(notificationIdAppender.append(
            existingNotifications,
            expectedNotificationReference,
            notificationId
        )).thenReturn(expectedNotifications);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            endAppealNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender).sendEmail(
            govNotifyTemplateIdConfiguration.getEndAppealHomeOfficeTemplateId(),
            HOME_OFFICE_EMAIL_ADDRESS,
            personalisation,
            expectedNotificationReference
        );

        verify(asylumCase).write(NOTIFICATIONS_SENT, expectedNotifications);
        verify(notificationIdAppender).append(existingNotifications, expectedNotificationReference, notificationId);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle_ho() {

        handlingShouldThrowIfCannotActuallyHandle(endAppealNotifier);
    }


    @Test
    public void it_can_handle_callback() {

        itCanHandleCallback(endAppealNotifier, Event.END_APPEAL);
    }

    @Test
    public void should_not_allow_null_arguments() {

        shouldNotAllowNullArguments(endAppealNotifier);
    }
}
