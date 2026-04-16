package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper.NotificationSenderHelper;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class GovNotifyNotificationSenderTest {

    private static final org.slf4j.Logger LOG = getLogger(GovNotifyNotificationSender.class);

    private final int deduplicateSendsWithinSeconds = 1;
    @Mock
    private RetryableNotificationClient notificationClient;

    @Mock
    private NotificationSenderHelper<AsylumCase> senderHelper;
    @Mock
    private InputStream stream;

    @Mock
    private Callback<AsylumCase> callback;
    private final String templateId = "a-b-c-d-e-f";
    private final String emailAddress = "recipient@example.com";
    private final Map<String, String> personalisation = mock(Map.class);
    private final Map<String, Object> personalisationWithLink = mock(Map.class);
    private final String reference = "our-reference";

    private GovNotifyNotificationSender govNotifyNotificationSender;

    @BeforeEach
    public void setUp() {
        govNotifyNotificationSender =
            new GovNotifyNotificationSender(
                deduplicateSendsWithinSeconds,
                notificationClient,
                senderHelper
            );
    }

    @Test
    public void should_send_email_using_appeal_gov_notify() {

        final UUID expectedNotificationId = UUID.randomUUID();

        when(senderHelper.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG,
            callback
        )).thenReturn(String.valueOf(expectedNotificationId));

        String actualNotificationId =
            govNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                callback
            );

        verify(senderHelper, times(1)).sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG,
            callback
        );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void should_send_sms_using_appeal_gov_notify() {

        final UUID expectedNotificationId = UUID.randomUUID();

        String phoneNumber = "07123456789";
        when(senderHelper.sendSms(
            templateId,
            phoneNumber,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG,
            callback
        )).thenReturn(String.valueOf(expectedNotificationId));

        String actualNotificationId =
            govNotifyNotificationSender.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference,
                callback
            );

        verify(senderHelper, times(1)).sendSms(
            templateId,
            phoneNumber,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG,
            callback
        );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void should_send_email_using_appeal_gov_notify_with_link() {

        final UUID expectedNotificationId = UUID.randomUUID();

        when(senderHelper.sendEmailWithLink(
            templateId,
            emailAddress,
            personalisationWithLink,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG
        )).thenReturn(String.valueOf(expectedNotificationId));

        String actualNotificationId =
            govNotifyNotificationSender.sendEmailWithLink(
                templateId,
                emailAddress,
                personalisationWithLink,
                reference
            );

        verify(senderHelper, times(1)).sendEmailWithLink(
            templateId,
            emailAddress,
            personalisationWithLink,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG
        );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void should_send_letter_using_gov_notify() {

        final UUID expectedNotificationId = UUID.randomUUID();

        String address = "20_realstreet_London";
        when(senderHelper.sendLetter(
            templateId,
            address,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG
        )).thenReturn(String.valueOf(expectedNotificationId));

        String actualNotificationId =
            govNotifyNotificationSender.sendLetter(
                templateId,
                address,
                personalisation,
                reference,
                callback
            );

        verify(senderHelper, times(1)).sendLetter(
            templateId,
            address,
            personalisation,
            reference,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG
        );
        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void should_send_precompiled_letter_using_gov_notify() {

        final UUID expectedNotificationId = UUID.randomUUID();

        when(senderHelper.sendPrecompiledLetter(
            reference,
            stream,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG
        )).thenReturn(String.valueOf(expectedNotificationId));

        String actualNotificationId =
            govNotifyNotificationSender.sendPrecompiledLetter(
                reference,
                stream
            );

        verify(senderHelper, times(1)).sendPrecompiledLetter(
            reference,
            stream,
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG
        );
        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }
}
