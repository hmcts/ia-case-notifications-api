package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class BailGovNotifyNotificationSenderTest {

    private int deduplicateSendsWithinSeconds = 1;

    @Mock
    @Qualifier("BailClient")
    private RetryableNotificationClient notificationBailClient;

    private String templateId = "a-b-c-d-e-f";
    private String emailAddress = "recipient@example.com";
    private String phoneNumber = "07123456789";
    private Map<String, String> personalisation = mock(Map.class);
    private String reference = "our-reference";

    private BailGovNotifyNotificationSender bailGovNotifyNotificationSender;

    @BeforeEach
    public void setUp() {
        bailGovNotifyNotificationSender =
            new BailGovNotifyNotificationSender(
                deduplicateSendsWithinSeconds,
                notificationBailClient
            );
    }

    @Test
    public void should_send_email_using_bail_gov_notify() throws NotificationClientException {

        final UUID expectedNotificationId = UUID.randomUUID();

        SendEmailResponse sendEmailResponse = mock(SendEmailResponse.class);

        when(notificationBailClient.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
        )).thenReturn(sendEmailResponse);

        when(sendEmailResponse.getNotificationId()).thenReturn(expectedNotificationId);

        String actualNotificationId =
                bailGovNotifyNotificationSender.sendEmail(
                        templateId,
                        emailAddress,
                        personalisation,
                        reference
                );

        verify(notificationBailClient, times(1)).sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
        );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void should_not_send_duplicate_emails_in_short_space_of_time() throws NotificationClientException {

        final String otherEmailAddress = "foo@bar.com";
        final String otherReference = "1111_SOME_OTHER_NOTIFICATION";

        final UUID expectedNotificationId = UUID.randomUUID();
        final UUID expectedNotificationIdForOther = UUID.randomUUID();

        SendEmailResponse sendEmailResponse = mock(SendEmailResponse.class);
        SendEmailResponse sendEmailResponseForOther = mock(SendEmailResponse.class);

        when(notificationBailClient.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference
        )).thenReturn(sendEmailResponse);

        when(notificationBailClient.sendEmail(
            templateId,
            otherEmailAddress,
            personalisation,
            otherReference
        )).thenReturn(sendEmailResponseForOther);

        when(sendEmailResponse.getNotificationId()).thenReturn(expectedNotificationId);
        when(sendEmailResponseForOther.getNotificationId()).thenReturn(expectedNotificationIdForOther);

        final String actualNotificationId1 =
            bailGovNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        final String actualNotificationId2 =
            bailGovNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        final String actualNotificationIdForOther =
            bailGovNotifyNotificationSender.sendEmail(
                templateId,
                otherEmailAddress,
                personalisation,
                otherReference
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
            bailGovNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId3);

        verify(notificationBailClient, times(2)).sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference
        );

        verify(notificationBailClient, times(1)).sendEmail(
            templateId,
            otherEmailAddress,
            personalisation,
            otherReference
        );
    }

    @Test
    public void wrap_bail_gov_notify_email_exceptions() throws NotificationClientException {

        NotificationClientException underlyingException = mock(NotificationClientException.class);

        doThrow(underlyingException)
            .when(notificationBailClient)
            .sendEmail(
                    templateId,
                    emailAddress,
                    personalisation,
                    reference
            );

        assertThatThrownBy(() ->
                bailGovNotifyNotificationSender.sendEmail(
                        templateId,
                        emailAddress,
                        personalisation,
                        reference
                )
        ).isExactlyInstanceOf(NotificationServiceResponseException.class)
                .hasMessage("Failed to send email using Bail GovNotify")
                .hasCause(underlyingException);

    }

    @Test
    public void should_send_sms_using_bail_gov_notify() throws NotificationClientException {

        final UUID expectedNotificationId = UUID.randomUUID();

        SendSmsResponse sendSmsResponse = mock(SendSmsResponse.class);

        when(notificationBailClient.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
        )).thenReturn(sendSmsResponse);

        when(sendSmsResponse.getNotificationId()).thenReturn(expectedNotificationId);

        String actualNotificationId =
                bailGovNotifyNotificationSender.sendSms(
                        templateId,
                        phoneNumber,
                        personalisation,
                        reference
                );

        verify(notificationBailClient, times(1)).sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
        );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void should_not_send_duplicate_sms_in_short_space_of_time() throws NotificationClientException {

        final String otherPhoneNumber = "07123456780";
        final String otherReference = "1111_SOME_OTHER_NOTIFICATION";

        final UUID expectedNotificationId = UUID.randomUUID();
        final UUID expectedNotificationIdForOther = UUID.randomUUID();

        SendSmsResponse sendSmsResponse = mock(SendSmsResponse.class);
        SendSmsResponse sendSmsResponseForOther = mock(SendSmsResponse.class);

        when(notificationBailClient.sendSms(
            templateId,
            phoneNumber,
            personalisation,
            reference
        )).thenReturn(sendSmsResponse);

        when(notificationBailClient.sendSms(
            templateId,
            otherPhoneNumber,
            personalisation,
            otherReference
        )).thenReturn(sendSmsResponseForOther);

        when(sendSmsResponse.getNotificationId()).thenReturn(expectedNotificationId);
        when(sendSmsResponseForOther.getNotificationId()).thenReturn(expectedNotificationIdForOther);

        final String actualNotificationId1 =
            bailGovNotifyNotificationSender.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
            );

        final String actualNotificationId2 =
            bailGovNotifyNotificationSender.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
            );

        final String actualNotificationIdForOther =
            bailGovNotifyNotificationSender.sendSms(
                templateId,
                otherPhoneNumber,
                personalisation,
                otherReference
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
            bailGovNotifyNotificationSender.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId3);

        verify(notificationBailClient, times(2)).sendSms(
            templateId,
            phoneNumber,
            personalisation,
            reference
        );

        verify(notificationBailClient, times(1)).sendSms(
            templateId,
            otherPhoneNumber,
            personalisation,
            otherReference
        );
    }

    @Test
    public void wrap_bail_gov_notify_sms_exceptions() throws NotificationClientException {

        NotificationClientException underlyingException = mock(NotificationClientException.class);

        doThrow(underlyingException)
            .when(notificationBailClient)
            .sendSms(
                    templateId,
                    phoneNumber,
                    personalisation,
                    reference
            );

        assertThatThrownBy(() ->
                bailGovNotifyNotificationSender.sendSms(
                        templateId,
                        phoneNumber,
                        personalisation,
                        reference
                )
        ).isExactlyInstanceOf(NotificationServiceResponseException.class)
                .hasMessage("Failed to send sms using Bail GovNotify")
                .hasCause(underlyingException);

    }

}
