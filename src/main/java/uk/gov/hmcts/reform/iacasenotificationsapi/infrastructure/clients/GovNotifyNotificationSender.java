package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import java.util.Map;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.UnrecoverableException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

@Service
public class GovNotifyNotificationSender implements NotificationSender {

    private final NotificationClient notificationClient;

    public GovNotifyNotificationSender(
        NotificationClient notificationClient
    ) {
        this.notificationClient = notificationClient;
    }

    public String sendEmail(
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        String reference
    ) {
        try {

            SendEmailResponse response =
                notificationClient
                    .sendEmail(
                        templateId,
                        emailAddress,
                        personalisation,
                        reference
                    );

            return response
                .getNotificationId()
                .toString();

        } catch (NotificationClientException e) {
            throw new UnrecoverableException("Failed to send email using GovNotify", e);
        }
    }
}
