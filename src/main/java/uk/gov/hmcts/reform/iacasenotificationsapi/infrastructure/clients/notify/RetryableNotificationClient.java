package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.notify;

import lombok.extern.slf4j.Slf4j;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

import java.util.Map;

@Slf4j
public class RetryableNotificationClient implements NotificationClientApi {

    private final NotificationClientApi notificationClient;

    public RetryableNotificationClient(NotificationClientApi notificationClient) {
        this.notificationClient = notificationClient;
    }

    @Override
    public SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        try {
            return notificationClient.sendEmail(templateId, emailAddress, personalisation, reference);
        } catch (NotificationClientException e) {
            log.warn("retry triggered for sendEmail: {}", e.getMessage());
            return notificationClient.sendEmail(templateId, emailAddress, personalisation, reference);
        }
    }

    @Override
    public SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, ?> personalisation, String reference, String emailReplyToId) throws NotificationClientException {
        try {
            return notificationClient.sendEmail(templateId, emailAddress, personalisation, emailReplyToId);
        } catch (NotificationClientException e) {
            log.warn("retry triggered for sendEmail: {}", e.getMessage());
            return notificationClient.sendEmail(templateId, emailAddress, personalisation, emailReplyToId);
        }

    }

    @Override
    public SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, ?> personalisation, String reference) throws NotificationClientException {
        try {
            return notificationClient.sendSms(templateId, phoneNumber, personalisation, reference);
        } catch (NotificationClientException e) {
            log.warn("retry triggered for sendSms: {}", e.getMessage());
            return notificationClient.sendSms(templateId, phoneNumber, personalisation, reference);
        }
    }

    @Override
    public SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, ?> personalisation, String reference, String smsSenderId) throws NotificationClientException {
        try {
            return notificationClient.sendSms(templateId, phoneNumber, personalisation, reference, smsSenderId);
        } catch (NotificationClientException e) {
            log.warn("retry triggered for sendSms: {}", e.getMessage());
            return notificationClient.sendSms(templateId, phoneNumber, personalisation, reference, smsSenderId);
        }
    }

    @Override
    public Notification getNotificationById(String notificationId) throws NotificationClientException {
        try {
            return notificationClient.getNotificationById(notificationId);
        } catch (NotificationClientException e) {
            log.warn("retry triggered for getNotifications: {}", e.getMessage());
            return notificationClient.getNotificationById(notificationId);
        }
    }
}
