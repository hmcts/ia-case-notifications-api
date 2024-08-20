package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.NotificationServiceResponseException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.RetryableNotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS;

@Component
public class NotificationSenderHelper<T extends CaseData> {

    private Cache<String, String> recentDeliveryReceiptCache;

    public String sendEmail(
            String templateId,
            String emailAddress,
            Map<String, String> personalisation,
            String reference,
            RetryableNotificationClient notificationClient,
            Integer deduplicateSendsWithinSeconds,
            Logger logger
    ) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache(deduplicateSendsWithinSeconds);
        return recentDeliveryReceiptCache.get(
                emailAddress + reference,
                k -> {
                    try {
                        logger.info("Attempting to send email notification to GovNotify: {}", reference);

                        SendEmailResponse response = notificationClient.sendEmail(
                                templateId,
                                emailAddress,
                                personalisation,
                                reference
                        );

                        String notificationId = response.getNotificationId().toString();

                        logger.info("Successfully sent email notification to GovNotify: {} ({})",
                                reference,
                                notificationId
                        );

                        return notificationId;

                    } catch (NotificationClientException e) {
                        logger.error("Failed to send email using GovNotify for case reference {}", reference, e);
                    }
                    return Strings.EMPTY;
                }
        );
    }

    public String sendEmailWithLink(
            String templateId,
            String emailAddress,
            Map<String, Object> personalisation,
            String reference,
            RetryableNotificationClient notificationClient,
            Integer deduplicateSendsWithinSeconds,
            Logger logger
    ) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache(deduplicateSendsWithinSeconds);
        return recentDeliveryReceiptCache.get(
                emailAddress + reference,
                k -> {
                    try {
                        logger.info("Attempting to send email notification to GovNotify: {}", reference);

                        SendEmailResponse response = notificationClient.sendEmail(
                                templateId,
                                emailAddress,
                                personalisation,
                                reference
                        );

                        String notificationId = response.getNotificationId().toString();

                        logger.info("Successfully sent email notification to GovNotify: {} ({})",
                                reference,
                                notificationId
                        );

                        return notificationId;

                    } catch (NotificationClientException e) {
                        throw new NotificationServiceResponseException("Failed to send email using GovNotify", e);
                    }
                }
        );
    }

    public String sendSms(
            String templateId,
            String phoneNumber,
            Map<String, String> personalisation,
            String reference,
            RetryableNotificationClient notificationClient,
            Integer deduplicateSendsWithinSeconds,
            Logger logger,
            Callback<T> callback
    ) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache(deduplicateSendsWithinSeconds);
        return recentDeliveryReceiptCache.get(
                phoneNumber + reference,
                k -> {
                    try {
                        logger.info("Attempting to send a text message notification to GovNotify: {}", reference);

                        SendSmsResponse response = notificationClient.sendSms(
                                templateId,
                                phoneNumber,
                                personalisation,
                                reference
                        );

                        String notificationId = response.getNotificationId().toString();

                        logger.info("Successfully sent sms notification to GovNotify: {} ({})",
                                reference,
                                notificationId
                        );

                        return notificationId;

                    } catch (NotificationClientException e) {
                        logger.error("Failed to send sms using GovNotify for case reference {}", reference, e);
                        String errorMessage = e.getMessage();
                        AsylumCase asylumCase = (AsylumCase) callback.getCaseDetails().getCaseData();
                        Optional<List<IdValue<StoredNotification>>> maybeExistingNotifications =
                            asylumCase.read(NOTIFICATIONS);
                        List<IdValue<StoredNotification>> allNotifications = maybeExistingNotifications.orElse(emptyList());
                        StoredNotification storedNotification = getFailedNotification(errorMessage, reference,
                            "sms", phoneNumber);
                        allNotifications = append(storedNotification, allNotifications);
                        asylumCase.write(NOTIFICATIONS, allNotifications);
                    }
                    return Strings.EMPTY;
                }
        );
    }

    private Cache<String, String> getOrCreateDeliveryReceiptCache(int deduplicateSendsWithinSeconds) {
        if (recentDeliveryReceiptCache == null) {
            recentDeliveryReceiptCache =
                    Caffeine
                            .newBuilder()
                            .expireAfterWrite(deduplicateSendsWithinSeconds, TimeUnit.SECONDS)
                            .build();
        }

        return recentDeliveryReceiptCache;
    }

    private static StoredNotification getFailedNotification(String errorMessage, String reference, String method,
                                                            String sentTo) {
        ZonedDateTime zonedSentAt = ZonedDateTime.now()
            .withZoneSameInstant(ZoneId.of("Europe/London"));
        String sentAt = zonedSentAt.toLocalDateTime().toString();
        String subject = "N/A";
        return StoredNotification.builder()
            .notificationId("N/A")
            .notificationDateSent(sentAt)
            .notificationSentTo(sentTo)
            .notificationBody(errorMessage)
            .notificationMethod(method)
            .notificationStatus("Failed")
            .notificationReference(reference)
            .notificationSubject(subject)
            .build();
    }

    private List<IdValue<StoredNotification>> append(
        StoredNotification newItem,
        List<IdValue<StoredNotification>> existingItems
    ) {

        requireNonNull(newItem);

        final List<IdValue<StoredNotification>> allItems = new ArrayList<>();

        int index = existingItems.size() + 1;

        IdValue<StoredNotification> itemIdValue = new IdValue<>(String.valueOf(index--), newItem);

        allItems.add(itemIdValue);

        for (IdValue<StoredNotification> existingItem : existingItems) {
            allItems.add(new IdValue<>(
                String.valueOf(index--),
                existingItem.getValue()));
        }

        return allItems;
    }
}
