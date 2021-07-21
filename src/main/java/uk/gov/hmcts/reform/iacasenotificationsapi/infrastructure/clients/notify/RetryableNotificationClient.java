package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.notify;

import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

@Slf4j
public class RetryableNotificationClient implements NotificationClientApi {

    private final NotificationClientApi notificationClient;

    public RetryableNotificationClient(NotificationClientApi notificationClient) {
        this.notificationClient = notificationClient;
    }

    @Override
    public SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, ?> personalisation, String reference) throws NotificationClientException {

        return retryableExecution(() -> notificationClient.sendEmail(templateId, emailAddress, personalisation, reference));
    }

    @Override
    public SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, ?> personalisation, String reference) throws NotificationClientException {

        return retryableExecution(() -> notificationClient.sendSms(templateId, phoneNumber, personalisation, reference));
    }

    @Override
    public Notification getNotificationById(String notificationId) throws NotificationClientException {

        return retryableExecution(() -> notificationClient.getNotificationById(notificationId));
    }

    private <T> T retryableExecution(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (NotificationClientException e) {
            log.warn("retry triggered: {}", e.getMessage());
            return supplier.get();
        }
    }
}
