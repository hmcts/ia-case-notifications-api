package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.notify;

import java.util.Map;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

public interface NotificationClientApi {

    /**
     * The sendEmail method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible on the template page in the application.
     * @param emailAddress    The email address
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @return <code>SendEmailResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#send-an-email-error-codes
     */
    SendEmailResponse sendEmail(String templateId, String emailAddress, Map<String, ?> personalisation, String reference);

    /**
     * The sendSms method will create an HTTPS POST request. A JWT token will be created and added as an Authorization header to the request.
     *
     * @param templateId      The template id is visible from the template page in the application.
     * @param phoneNumber     The mobile phone number
     * @param personalisation Map representing the placeholders for the template if any. For example, key=name value=Bob
     *                        Can be an empty map or null when the template does not require placeholders.
     * @param reference       A reference specified by the service for the notification. Get all notifications can be filtered by this reference.
     *                        This reference can be unique or used used to refer to a batch of notifications.
     *                        Can be an empty string or null, when you do not require a reference for the notifications.
     * @return <code>SendSmsResponse</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#error-codes
     */
    SendSmsResponse sendSms(String templateId, String phoneNumber, Map<String, ?> personalisation, String reference);

    /**
     * The getNotificationById method will return a <code>Notification</code> for a given notification id.
     * The id can be retrieved from the <code>NotificationResponse</code> of a <code>sendEmail</code>, <code>sendLetter</code> or <code>sendSms</code> request.
     *
     * @param notificationId The id of the notification.
     * @return <code>Notification</code>
     * @throws NotificationClientException see https://docs.notifications.service.gov.uk/java.html#get-the-status-of-one-message-error-codes
     */
    Notification getNotificationById(String notificationId);

}
