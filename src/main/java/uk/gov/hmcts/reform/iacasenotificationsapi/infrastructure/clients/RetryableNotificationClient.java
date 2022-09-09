package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import uk.gov.service.notify.*;

@Slf4j
public class RetryableNotificationClient {

    private final NotificationClientApi notificationClient;

    public RetryableNotificationClient(NotificationClientApi notificationClient) {
        this.notificationClient = notificationClient;
    }

    public SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        try {
            return notificationClient.sendEmail(templateId, emailAddress, personalisation, reference);
        } catch (NotificationClientException e) {
            log.warn("retry triggered: {}", e.getMessage());
            return notificationClient.sendEmail(templateId, emailAddress, personalisation, reference);
        }
    }

    public SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        try {
            return notificationClient.sendSms(templateId, phoneNumber, personalisation, reference);
        } catch (NotificationClientException e) {
            log.warn("retry triggered: {}", e.getMessage());
            return notificationClient.sendSms(templateId, phoneNumber, personalisation, reference);
        }
    }

    public SendLetterResponse sendLetter(String templateId, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        try {
            return notificationClient.sendLetter(templateId, personalisation, reference);
        } catch (NotificationClientException e) {
            log.warn("retry triggered: {}", e.getMessage());
            return notificationClient.sendLetter(templateId, personalisation, reference);
        }
    }

    public Notification getNotificationById(String notificationId) throws NotificationClientException {
        try {
            return notificationClient.getNotificationById(notificationId);
        } catch (NotificationClientException e) {
            log.warn("retry triggered: {}", e.getMessage());
            return notificationClient.getNotificationById(notificationId);
        }
    }

}
