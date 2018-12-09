package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.UnrecoverableException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class GovNotifyNotificationSenderTest {

    @Mock private NotificationClient notificationClient;

    private String templateId = "a-b-c-d-e-f";
    private String emailAddress = "recipient@example.com";
    private Map<String, String> personalisation = mock(Map.class);
    private String reference = "our-reference";

    private GovNotifyNotificationSender govNotifyNotificationSender;

    @Before
    public void setUp() {
        govNotifyNotificationSender =
            new GovNotifyNotificationSender(
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
    public void wrap_gov_notify_exceptions() throws NotificationClientException {

        doThrow(NotificationClientException.class)
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
        ).hasMessage("Failed to send email using GovNotify")
            .isExactlyInstanceOf(UnrecoverableException.class);
    }
}
