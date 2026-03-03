package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.HashMap;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.NotificationServiceResponseException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.RetryableNotificationClient;
import uk.gov.service.notify.*;

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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class NotificationSenderHelperTest {

    private static final org.slf4j.Logger LOG = getLogger(NotificationSenderHelperTest.class);

    private static class UnknownCase extends HashMap<String, Object> implements CaseData {
        // noop
    }

    @Mock
    private RetryableNotificationClient notificationClient;
    @Mock
    private Callback<AsylumCase> asylumCallback;
    @Mock
    private CaseDetails<AsylumCase> asylumCaseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Callback<BailCase> bailCallback;
    @Mock
    private CaseDetails<BailCase> bailCaseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private Callback<UnknownCase> unknownCallback;
    @Mock
    private CaseDetails<UnknownCase> unknownCaseDetails;
    @Mock
    private UnknownCase unknownCase;
    @Mock
    private StoredNotification storedNotificationMock;
    @Mock
    private StoredNotification storedNotificationMock2;

    private NotificationSenderHelper<AsylumCase> senderHelper = new NotificationSenderHelper<AsylumCase>();
    private NotificationSenderHelper<BailCase> bailSenderHelper = new NotificationSenderHelper<BailCase>();
    private NotificationSenderHelper<UnknownCase> unknownSenderHelper = new NotificationSenderHelper<UnknownCase>();

    @Mock
    private InputStream stream;

    private int deduplicateSendsWithinSeconds = 1;
    private String templateId = "a-b-c-d-e-f";
    private String emailAddress = "recipient@example.com";
    private String phoneNumber = "07123456789";
    private String address = "20_realstreet_London";
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
                LOG,
                asylumCallback
            );

        final String actualNotificationId2 =
            senderHelper.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG,
                asylumCallback
            );

        final String actualNotificationIdForOther =
            senderHelper.sendEmail(
                templateId,
                otherEmailAddress,
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
            senderHelper.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG,
                asylumCallback
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
        when(asylumCallback.getCaseDetails()).thenReturn(asylumCaseDetails);
        when(asylumCaseDetails.getCaseData()).thenReturn(asylumCase);
        String actualNotificationId = senderHelper.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG,
            asylumCallback
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
    public void wrap_gov_notify_sms_exceptions_and_sort_notifications_list() throws NotificationClientException {
        when(asylumCallback.getCaseDetails()).thenReturn(asylumCaseDetails);
        when(asylumCaseDetails.getCaseData()).thenReturn(asylumCase);
        List<IdValue<StoredNotification>> someList = List.of(
            new IdValue<>("some-id", storedNotificationMock),
            new IdValue<>("some-id-2", storedNotificationMock2)
        );
        when(storedNotificationMock.getNotificationDateSent()).thenReturn("2024-01-01T00:00:00");
        when(storedNotificationMock2.getNotificationDateSent()).thenReturn("2024-01-01T00:05:00");
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
        ArgumentCaptor<List<IdValue<StoredNotification>>> captor = ArgumentCaptor.forClass(List.class);

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
        verify(asylumCase).write(eq(NOTIFICATIONS), captor.capture());
        List<IdValue<StoredNotification>> capturedList = captor.getValue();
        assertNotNull(capturedList);
        assertEquals(3, capturedList.size());
        assertEquals(capturedList.get(1).getValue(), storedNotificationMock2);
        assertEquals(capturedList.get(2).getValue(), storedNotificationMock);
    }

    @Test
    public void should_not_send_duplicate_leter_in_short_space_of_time() throws NotificationClientException {

        final String otherAddress = "44_realstreet_London";
        final String otherReference = "1111_SOME_OTHER_NOTIFICATION";

        final UUID expectedNotificationId = UUID.randomUUID();
        final UUID expectedNotificationIdForOther = UUID.randomUUID();

        SendLetterResponse sendLetterResponse = mock(SendLetterResponse.class);
        SendLetterResponse sendLetterResponseForOther = mock(SendLetterResponse.class);

        when(notificationClient.sendLetter(
            templateId,
            personalisation,
            reference
        )).thenReturn(sendLetterResponse);

        when(notificationClient.sendLetter(
            templateId,
            personalisation,
            otherReference
        )).thenReturn(sendLetterResponseForOther);

        when(sendLetterResponse.getNotificationId()).thenReturn(expectedNotificationId);
        when(sendLetterResponseForOther.getNotificationId()).thenReturn(expectedNotificationIdForOther);

        final String actualNotificationId1 =
            senderHelper.sendLetter(
                templateId,
                address,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        final String actualNotificationId2 =
            senderHelper.sendLetter(
                templateId,
                address,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        final String actualNotificationIdForOther =
            senderHelper.sendLetter(
                templateId,
                otherAddress,
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
            senderHelper.sendLetter(
                templateId,
                address,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId3);

        verify(notificationClient, times(2)).sendLetter(
            templateId,
            personalisation,
            reference
        );

        verify(notificationClient, times(1)).sendLetter(
            templateId,
            personalisation,
            otherReference
        );
    }

    @Test
    public void wrap_gov_notify_letter_exceptions() throws NotificationClientException {
        NotificationClientException underlyingException = mock(NotificationClientException.class);

        doThrow(underlyingException)
            .when(notificationClient)
            .sendLetter(
                templateId,
                personalisation,
                reference
            );

        String actualNotificationId = senderHelper.sendLetter(
            templateId,
            address,
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
    public void should_not_send_duplicate_precompiled_leters_in_short_space_of_time() throws NotificationClientException {

        final String otherReference = "1111_SOME_OTHER_NOTIFICATION";

        final UUID expectedNotificationId = UUID.randomUUID();
        final UUID expectedNotificationIdForOther = UUID.randomUUID();

        LetterResponse letterResponse = mock(LetterResponse.class);
        LetterResponse letterResponseForOther = mock(LetterResponse.class);

        when(notificationClient.sendPrecompiledLetter(
            reference,
            stream
        )).thenReturn(letterResponse);

        when(notificationClient.sendPrecompiledLetter(
            otherReference,
            stream
        )).thenReturn(letterResponseForOther);

        when(letterResponse.getNotificationId()).thenReturn(expectedNotificationId);
        when(letterResponseForOther.getNotificationId()).thenReturn(expectedNotificationIdForOther);

        final String actualNotificationId1 =
            senderHelper.sendPrecompiledLetter(
                reference,
                stream,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        final String actualNotificationId2 =
            senderHelper.sendPrecompiledLetter(
                reference,
                stream,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        final String actualNotificationIdForOther =
            senderHelper.sendPrecompiledLetter(
                otherReference,
                stream,
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
            senderHelper.sendPrecompiledLetter(
                reference,
                stream,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId3);

        verify(notificationClient, times(2)).sendPrecompiledLetter(
            reference,
            stream
        );

        verify(notificationClient, times(1)).sendPrecompiledLetter(
            otherReference,
            stream
        );
    }

    @Test
    public void wrap_gov_notify_precompiled_letter_exceptions() throws NotificationClientException {
        NotificationClientException underlyingException = mock(NotificationClientException.class);

        doThrow(underlyingException)
            .when(notificationClient)
            .sendPrecompiledLetter(
                reference,
                stream
            );

        String actualNotificationId = senderHelper.sendPrecompiledLetter(
            reference,
            stream,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG
        );

        assertNotNull(actualNotificationId);
        assertTrue(actualNotificationId.isEmpty());
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


    @Test
    void storeFailedNotification_should_store_for_asylum_case() throws Exception {
        when(asylumCallback.getCaseDetails()).thenReturn(asylumCaseDetails);
        when(asylumCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.NOTIFICATIONS)).thenReturn(Optional.empty());
        String exceptionMessage = "Status code: 400 {\"errors\":[{\"error\":\"InvalidPhoneError\",\"message\":\"some error message\"}],\"status_code\":400}";
        NotificationClientException exception = new NotificationClientException(exceptionMessage);
        when(notificationClient.sendEmail(any(), any(), any(), any())).thenThrow(exception);
        senderHelper.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG,
            asylumCallback
        );

        ArgumentCaptor<List<IdValue<StoredNotification>>> captor = ArgumentCaptor.forClass(List.class);
        verify(asylumCase).write(eq(AsylumCaseDefinition.NOTIFICATIONS), captor.capture());
        List<IdValue<StoredNotification>> notifications = captor.getValue();
        assertEquals(1, notifications.size());
        StoredNotification storedNotification = notifications.get(0).getValue();
        assertEquals("N/A", storedNotification.getNotificationId());
        assertEquals(emailAddress, storedNotification.getNotificationSentTo());
        assertEquals("N/A", storedNotification.getNotificationBody());
        assertEquals("Email", storedNotification.getNotificationMethod());
        assertEquals("Failed", storedNotification.getNotificationStatus());
        assertEquals(reference, storedNotification.getNotificationReference());
        assertEquals(reference, storedNotification.getNotificationSubject());
        assertEquals("Some error message", storedNotification.getNotificationErrorMessage());
    }

    @Test
    void storeFailedNotification_should_store_for_bail_case() throws Exception {
        when(bailCallback.getCaseDetails()).thenReturn(bailCaseDetails);
        when(bailCaseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BailCaseFieldDefinition.NOTIFICATIONS)).thenReturn(Optional.empty());
        String exceptionMessage = "Status code: 400 {\"errors\":[{\"error\":\"InvalidPhoneError\",\"message\":\"some error message\"}],\"status_code\":400}";
        NotificationClientException exception = new NotificationClientException(exceptionMessage);
        when(notificationClient.sendEmail(any(), any(), any(), any())).thenThrow(exception);
        bailSenderHelper.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG,
            bailCallback
        );

        ArgumentCaptor<List<IdValue<StoredNotification>>> captor = ArgumentCaptor.forClass(List.class);
        verify(bailCase).write(eq(BailCaseFieldDefinition.NOTIFICATIONS), captor.capture());
        List<IdValue<StoredNotification>> notifications = captor.getValue();
        assertEquals(1, notifications.size());
        StoredNotification storedNotification = notifications.get(0).getValue();
        assertEquals("N/A", storedNotification.getNotificationId());
        assertEquals(emailAddress, storedNotification.getNotificationSentTo());
        assertEquals("N/A", storedNotification.getNotificationBody());
        assertEquals("Email", storedNotification.getNotificationMethod());
        assertEquals("Failed", storedNotification.getNotificationStatus());
        assertEquals(reference, storedNotification.getNotificationReference());
        assertEquals(reference, storedNotification.getNotificationSubject());
        assertEquals("Some error message", storedNotification.getNotificationErrorMessage());
    }

    @Test
    void storeFailedNotification_should_log_for_unsupported_type() throws NotificationClientException {
        Logger responseLogger = (Logger) LOG;
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        responseLogger.addAppender(listAppender);
        when(unknownCallback.getCaseDetails()).thenReturn(unknownCaseDetails);
        when(unknownCaseDetails.getCaseData()).thenReturn(unknownCase);
        NotificationClientException exception = new NotificationClientException("some error message");
        when(notificationClient.sendEmail(any(), any(), any(), any())).thenThrow(exception);
        unknownSenderHelper.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            responseLogger,
            unknownCallback
        );
        List<ILoggingEvent> logEvents = listAppender.list;
        assertEquals(3, logEvents.size());
        assertEquals("Attempting to send email notification to GovNotify: our-reference",
            logEvents.get(0).getFormattedMessage());
        assertEquals("Failed to send email using GovNotify for case reference our-reference",
            logEvents.get(1).getFormattedMessage());
        assertEquals("Unsupported case data type for storing failed notification: " +
                "uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper.NotificationSenderHelperTest$UnknownCase",
            logEvents.get(2).getFormattedMessage());
        verifyNoMoreInteractions(asylumCase, bailCase);
    }

    @Test
    void getSortedNotifications_should_append_and_sort() throws NotificationClientException {
        StoredNotification.StoredNotificationBuilder notificationBuilder = StoredNotification.builder()
            .notificationStatus("Failed")
            .notificationSentTo("some-email")
            .notificationBody("some-body")
            .notificationMethod("email")
            .notificationSubject("some-subject");

        StoredNotification pastNotification = notificationBuilder
            .notificationId("past")
            .notificationDateSent(ZonedDateTime.now().minusYears(1).toLocalDateTime().toString())
            .notificationReference("past-ref")
            .build();
        StoredNotification futureNotification = notificationBuilder
            .notificationId("future")
            .notificationDateSent(ZonedDateTime.now().plusYears(1).toLocalDateTime().toString())
            .notificationReference("future-ref")
            .build();
        IdValue<StoredNotification> pastIdValue = new IdValue<>("1", pastNotification);
        IdValue<StoredNotification> futureIdValue = new IdValue<>("2", futureNotification);

        List<IdValue<StoredNotification>> maybeExisting = List.of(pastIdValue, futureIdValue);
        when(asylumCallback.getCaseDetails()).thenReturn(asylumCaseDetails);
        when(asylumCaseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.NOTIFICATIONS)).thenReturn(Optional.of(maybeExisting));
        NotificationClientException exception = new NotificationClientException("some error message");
        when(notificationClient.sendEmail(any(), any(), any(), any())).thenThrow(exception);

        senderHelper.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG,
            asylumCallback
        );

        ArgumentCaptor<List<IdValue<StoredNotification>>> captor = ArgumentCaptor.forClass(List.class);
        verify(asylumCase).write(eq(AsylumCaseDefinition.NOTIFICATIONS), captor.capture());
        List<IdValue<StoredNotification>> notifications = captor.getValue();
        assertEquals(3, notifications.size());
        assertEquals("future-ref", notifications.get(0).getValue().getNotificationReference());
        assertEquals(reference, notifications.get(1).getValue().getNotificationReference());
        assertEquals("past-ref", notifications.get(2).getValue().getNotificationReference());
    }
}
