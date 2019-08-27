package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EditCaseListingPersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class EditCaseListingNotifierTest extends BaseNotifierTest {

    private static final String EDIT_CASE_LISTING_HOME_OFFICE_TEMPLATE = "edit-case-listing-home-office-template-id";
    private static final String EDIT_CASE_LISTING_LEGAL_REPRESENTATIVE_TEMPLATE = "edit-case-listing-legal-representative-template-id";

    @Mock private GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;

    @Mock private EditCaseListingPersonalisationFactory editCaseListingPersonalisationFactory;

    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private AsylumCase asylumCaseBefore;

    private final String expectedNotificationReference = caseId + "_EDIT_CASE_LISTING_HOME_OFFICE";

    private EditCaseListingNotifier editCaseListingNotifier;

    @Before
    public void setUp() {

        baseNotifierTestSetUp(Event.EDIT_CASE_LISTING);

        when(govNotifyTemplateIdConfiguration.getEditCaseListingHomeOfficeTemplateId()).thenReturn(EDIT_CASE_LISTING_HOME_OFFICE_TEMPLATE);

        editCaseListingNotifier =
                new EditCaseListingNotifier(
                        govNotifyTemplateIdConfiguration,
                        HOME_OFFICE_EMAIL_ADDRESS,
                        notificationSender,
                        notificationIdAppender,
                        editCaseListingPersonalisationFactory
                );

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
        when(editCaseListingPersonalisationFactory.create(asylumCase, asylumCaseBefore)).thenReturn(personalisation);

        when(notificationSender.sendEmail(
                EDIT_CASE_LISTING_HOME_OFFICE_TEMPLATE,
                HOME_OFFICE_EMAIL_ADDRESS,
                personalisation,
                expectedNotificationReference
        )).thenReturn(notificationId);
    }

    @Test
    public void should_send_edit_case_listing_notification_to_home_office() {

        final List<IdValue<String>> existingNotifications =
                new ArrayList<>(Collections.singletonList(
                        new IdValue<>("edit-case-listing-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ")
                ));

        final List<IdValue<String>> expectedNotifications =
                new ArrayList<>(Arrays.asList(
                        new IdValue<>("edit-case-listing-notification-sent", "ZZZ-ZZZ-ZZZ-ZZZ"),
                        new IdValue<>(expectedNotificationReference, notificationId)
                ));

        when(asylumCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(existingNotifications));

        when(notificationIdAppender.append(
                existingNotifications,
                expectedNotificationReference,
                notificationId
        )).thenReturn(expectedNotifications);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                editCaseListingNotifier.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(notificationSender).sendEmail(
                EDIT_CASE_LISTING_HOME_OFFICE_TEMPLATE,
                HOME_OFFICE_EMAIL_ADDRESS,
                personalisation,
                expectedNotificationReference
        );

        verify(asylumCase, times(1)).write(NOTIFICATIONS_SENT, expectedNotifications);
        verify(notificationIdAppender).append(existingNotifications, expectedNotificationReference, notificationId);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle_ho() {

        handlingShouldThrowIfCannotActuallyHandle(editCaseListingNotifier);
    }

    @Test
    public void it_can_handle_callback() {

        itCanHandleCallback(editCaseListingNotifier, Event.EDIT_CASE_LISTING);
    }

    @Test
    public void should_not_allow_null_arguments() {

        shouldNotAllowNullArguments(editCaseListingNotifier);
    }

}
