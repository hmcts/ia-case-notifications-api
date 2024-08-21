package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper;

import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.NotificationServiceResponseException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.RetryableNotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class NotificationSenderHelperTest {

    private static final org.slf4j.Logger LOG = getLogger(NotificationSenderHelperTest.class);

    @Mock
    private RetryableNotificationClient notificationClient;
    @Mock
    private Callback<AsylumCase> asylumCallback;
    @Mock
    private CaseDetails<AsylumCase> asylumCaseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private StoredNotification storedNotificationMock;

    private NotificationSenderHelper<AsylumCase> senderHelper = new NotificationSenderHelper<AsylumCase>();

    private int deduplicateSendsWithinSeconds = 1;
    private String templateId = "a-b-c-d-e-f";
    private String emailAddress = "recipient@example.com";
    private String phoneNumber = "07123456789";
    private Map<String, String> personalisation = mock(Map.class);
    private Map<String, Object> personalisationWithLink = mock(Map.class);
    private String reference = "our-reference";

    @Test
    public void should_not_send_duplicate_emails_in_short_space_of_time() throws NotificationClientException {

        final String otherEmailAddress = "foo@bar.com";
        final String otherReference = "1111_SOME_OTHER_NOTIFICATION";

        final UUID expectedNotificationId = UUID.randomUUID();
        final UUID expectedNotificationIdForOther = UUID.randomUUID();

        SendEmailResponse sendEmailResponse = mock(SendEmailResponse.class);
        SendEmailResponse sendEmailResponseForOther = mock(SendEmailResponse.class);

        when(notificationClient.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference
        )).thenReturn(sendEmailResponse);

        when(notificationClient.sendEmail(
            templateId,
            otherEmailAddress,
            personalisation,
            otherReference
        )).thenReturn(sendEmailResponseForOther);

        when(sendEmailResponse.getNotificationId()).thenReturn(expectedNotificationId);
        when(sendEmailResponseForOther.getNotificationId()).thenReturn(expectedNotificationIdForOther);

        final String actualNotificationId1 =
            senderHelper.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        final String actualNotificationId2 =
            senderHelper.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        final String actualNotificationIdForOther =
            senderHelper.sendEmail(
                templateId,
                otherEmailAddress,
                personalisation,
                otherReference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );


        assertEquals(expectedNotificationId.toString(), actualNotificationId1);
        assertEquals(expectedNotificationId.toString(), actualNotificationId2);
        assertEquals(expectedNotificationIdForOther.toString(), actualNotificationIdForOther);

        try {
            await().atMost(2, TimeUnit.SECONDS).until(() -> false);
        } catch (ConditionTimeoutException e) {
            assertTrue(true, "We expect this to timeout");
        }

        final String actualNotificationId3 =
            senderHelper.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId3);

        verify(notificationClient, times(2)).sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference
        );

        verify(notificationClient, times(1)).sendEmail(
            templateId,
            otherEmailAddress,
            personalisation,
            otherReference
        );
    }

    @Test
    public void wrap_gov_notify_email_exceptions() throws NotificationClientException {

        NotificationClientException underlyingException = mock(NotificationClientException.class);

        doThrow(underlyingException)
            .when(notificationClient)
            .sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        String actualNotificationId = senderHelper.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG
        );

        assertNotNull(actualNotificationId);
        assertTrue(actualNotificationId.isEmpty());
    }

    @Test
    public void should_not_send_duplicate_sms_in_short_space_of_time() throws NotificationClientException {

        final String otherPhoneNumber = "07123456780";
        final String otherReference = "1111_SOME_OTHER_NOTIFICATION";

        final UUID expectedNotificationId = UUID.randomUUID();
        final UUID expectedNotificationIdForOther = UUID.randomUUID();

        SendSmsResponse sendSmsResponse = mock(SendSmsResponse.class);
        SendSmsResponse sendSmsResponseForOther = mock(SendSmsResponse.class);

        when(notificationClient.sendSms(
            templateId,
            phoneNumber,
            personalisation,
            reference
        )).thenReturn(sendSmsResponse);

        when(notificationClient.sendSms(
            templateId,
            otherPhoneNumber,
            personalisation,
            otherReference
        )).thenReturn(sendSmsResponseForOther);

        when(sendSmsResponse.getNotificationId()).thenReturn(expectedNotificationId);
        when(sendSmsResponseForOther.getNotificationId()).thenReturn(expectedNotificationIdForOther);

        final String actualNotificationId1 =
            senderHelper.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG,
                asylumCallback
            );

        final String actualNotificationId2 =
            senderHelper.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG,
                asylumCallback
            );

        final String actualNotificationIdForOther =
            senderHelper.sendSms(
                templateId,
                otherPhoneNumber,
                personalisation,
                otherReference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG,
                asylumCallback
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId1);
        assertEquals(expectedNotificationId.toString(), actualNotificationId2);
        assertEquals(expectedNotificationIdForOther.toString(), actualNotificationIdForOther);

        try {
            await().atMost(2, TimeUnit.SECONDS).until(() -> false);
        } catch (ConditionTimeoutException e) {
            assertTrue(true, "We expect this to timeout");
        }

        final String actualNotificationId3 =
            senderHelper.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG,
                asylumCallback
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId3);

        verify(notificationClient, times(2)).sendSms(
            templateId,
            phoneNumber,
            personalisation,
            reference
        );

        verify(notificationClient, times(1)).sendSms(
            templateId,
            otherPhoneNumber,
            personalisation,
            otherReference
        );
    }

    @Test
    public void wrap_gov_notify_sms_exceptions() throws NotificationClientException {
        when(asylumCallback.getCaseDetails()).thenReturn(asylumCaseDetails);
        when(asylumCaseDetails.getCaseData()).thenReturn(asylumCase);
        List<IdValue<StoredNotification>> someList = List.of(new IdValue<>("some-id", storedNotificationMock));
        when(asylumCase.read(NOTIFICATIONS)).thenReturn(Optional.of(someList));
        NotificationClientException underlyingException = mock(NotificationClientException.class);
        when(underlyingException.getMessage()).thenReturn("some-error-message");
        doThrow(underlyingException)
            .when(notificationClient)
            .sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
            );

        String actualNotificationId = senderHelper.sendSms(
            templateId,
            phoneNumber,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG,
            asylumCallback
        );

        assertNotNull(actualNotificationId);
        assertTrue(actualNotificationId.isEmpty());
        verify(asylumCase).write(eq(NOTIFICATIONS), anyList());
    }

    @Test
    public void should_not_send_duplicate_emails_in_short_space_of_time_with_links() throws NotificationClientException {

        final String otherEmailAddress = "foo@bar.com";
        final String otherReference = "1111_SOME_OTHER_NOTIFICATION";

        final UUID expectedNotificationId = UUID.randomUUID();
        final UUID expectedNotificationIdForOther = UUID.randomUUID();

        SendEmailResponse sendEmailResponse = mock(SendEmailResponse.class);
        SendEmailResponse sendEmailResponseForOther = mock(SendEmailResponse.class);

        when(notificationClient.sendEmail(
            templateId,
            emailAddress,
            personalisationWithLink,
            reference
        )).thenReturn(sendEmailResponse);

        when(notificationClient.sendEmail(
            templateId,
            otherEmailAddress,
            personalisationWithLink,
            otherReference
        )).thenReturn(sendEmailResponseForOther);

        when(sendEmailResponse.getNotificationId()).thenReturn(expectedNotificationId);
        when(sendEmailResponseForOther.getNotificationId()).thenReturn(expectedNotificationIdForOther);

        final String actualNotificationId1 =
            senderHelper.sendEmailWithLink(
                templateId,
                emailAddress,
                personalisationWithLink,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        final String actualNotificationId2 =
            senderHelper.sendEmailWithLink(
                templateId,
                emailAddress,
                personalisationWithLink,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        final String actualNotificationIdForOther =
            senderHelper.sendEmailWithLink(
                templateId,
                otherEmailAddress,
                personalisationWithLink,
                otherReference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );


        assertEquals(expectedNotificationId.toString(), actualNotificationId1);
        assertEquals(expectedNotificationId.toString(), actualNotificationId2);
        assertEquals(expectedNotificationIdForOther.toString(), actualNotificationIdForOther);

        try {
            await().atMost(2, TimeUnit.SECONDS).until(() -> false);
        } catch (ConditionTimeoutException e) {
            assertTrue(true, "We expect this to timeout");
        }

        final String actualNotificationId3 =
            senderHelper.sendEmailWithLink(
                templateId,
                emailAddress,
                personalisationWithLink,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId3);

        verify(notificationClient, times(2)).sendEmail(
            templateId,
            emailAddress,
            personalisationWithLink,
            reference
        );

        verify(notificationClient, times(1)).sendEmail(
            templateId,
            otherEmailAddress,
            personalisationWithLink,
            otherReference
        );
    }

    @Test
    public void when_personalisation_with_links_wrap_gov_notify_email_exceptions() throws NotificationClientException {

        NotificationClientException underlyingException = mock(NotificationClientException.class);

        doThrow(underlyingException)
            .when(notificationClient)
            .sendEmail(
                templateId,
                emailAddress,
                personalisationWithLink,
                reference);

        assertThatThrownBy(() ->
            senderHelper.sendEmailWithLink(
                templateId,
                emailAddress,
                personalisationWithLink,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            )
        ).isExactlyInstanceOf(NotificationServiceResponseException.class)
            .hasMessage("Failed to send email using GovNotify")
            .hasCause(underlyingException);

    }

    @Test
    void testExtractErrorMessages_ValidInput() {
        String exceptionMessage = "Status code: 400 {\"errors\":[{\"error\":\"InvalidPhoneError\",\"message\":\"Not a UK mobile number\"}],\"status_code\":400}";

        List<String> result = NotificationSenderHelper.extractErrorMessages(exceptionMessage);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Not a UK mobile number", result.get(0));
    }

    @Test
    void testExtractErrorMessages_MultipleErrors() {
        String exceptionMessage = "Status code: 400 {\"errors\":[{\"error\":\"InvalidPhoneError\",\"message\":\"Not a UK mobile number\"}," +
            "{\"error\":\"InvalidEmailError\",\"message\":\"Not a valid email address\"}],\"status_code\":400}";

        List<String> result = NotificationSenderHelper.extractErrorMessages(exceptionMessage);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Not a UK mobile number", result.get(0));
        assertEquals("Not a valid email address", result.get(1));
    }

    @Test
    void testExtractErrorMessages_EmptyErrorsArray() {
        String exceptionMessage = "Status code: 400 {\"errors\":[],\"status_code\":400}";

        List<String> result = NotificationSenderHelper.extractErrorMessages(exceptionMessage);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testExtractErrorMessages_NoErrorsField() {
        String exceptionMessage = "";

        List<String> result = NotificationSenderHelper.extractErrorMessages(exceptionMessage);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testExtractErrorMessages_InvalidJson() {
        String exceptionMessage = "Status code: 400 {\"errors\":";

        List<String> result = NotificationSenderHelper.extractErrorMessages(exceptionMessage);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testExtractErrorMessages_InvalidJsonFormat_NoCurlyBrace() {
        String exceptionMessage = "Status code: 400 Invalid JSON";

        List<String> result = NotificationSenderHelper.extractErrorMessages(exceptionMessage);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testExtractErrorMessages_JsonWithoutMessageField() {
        String exceptionMessage = "Status code: 400 {\"errors\":[{\"error\":\"InvalidPhoneError\"}],\"status_code\":400}";

        List<String> result = NotificationSenderHelper.extractErrorMessages(exceptionMessage);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }
}
