package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class GovNotifyNotificationSenderTest {

    private int deduplicateSendsWithinSeconds = 1;
    @Mock
    private NotificationClient notificationClient;

    private String templateId = "a-b-c-d-e-f";
    private String emailAddress = "recipient@example.com";
    private String phoneNumber = "07123456789";
    private Map<String, String> personalisation = mock(Map.class);
    private String reference = "our-reference";

    private GovNotifyNotificationSender govNotifyNotificationSender;

    @BeforeEach
    public void setUp() {
        govNotifyNotificationSender =
            new GovNotifyNotificationSender(
                deduplicateSendsWithinSeconds,
                notificationClient
            );
    }

    @Test
    public void should_send_email_using_gov_notify() throws NotificationClientException {

        final UUID expectedNotificationId = UUID.randomUUID();

        SendEmailResponse sendEmailResponse = mock(SendEmailResponse.class);

        when(notificationClient.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference
        )).thenReturn(sendEmailResponse);

        when(sendEmailResponse.getNotificationId()).thenReturn(expectedNotificationId);

        String actualNotificationId =
            govNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
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

        assertThatThrownBy(() ->
            govNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            )
        ).isExactlyInstanceOf(NotificationServiceResponseException.class)
            .hasMessage("Failed to send email using GovNotify")
            .hasCause(underlyingException);

    }

    @Test
    public void should_send_sms_using_gov_notify() throws NotificationClientException {

        final UUID expectedNotificationId = UUID.randomUUID();

        SendSmsResponse sendSmsResponse = mock(SendSmsResponse.class);

        when(notificationClient.sendSms(
            templateId,
            phoneNumber,
            personalisation,
            reference
        )).thenReturn(sendSmsResponse);

        when(sendSmsResponse.getNotificationId()).thenReturn(expectedNotificationId);

        String actualNotificationId =
            govNotifyNotificationSender.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void wrap_gov_notify_sms_exceptions() throws NotificationClientException {

        NotificationClientException underlyingException = mock(NotificationClientException.class);

        doThrow(underlyingException)
            .when(notificationClient)
            .sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
            );

        assertThatThrownBy(() ->
            govNotifyNotificationSender.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
            )
        ).isExactlyInstanceOf(NotificationServiceResponseException.class)
            .hasMessage("Failed to send sms using GovNotify")
            .hasCause(underlyingException);

    }

}
