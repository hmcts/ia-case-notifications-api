package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.slf4j.LoggerFactory.getLogger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.CaseType;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

@Service
public class BailGovNotifyNotificationSender implements NotificationSender {

    private static final org.slf4j.Logger LOG = getLogger(BailGovNotifyNotificationSender.class);

    private final int deduplicateSendsWithinSeconds;

    @Autowired
    @Qualifier("BailClient")
    private final RetryableNotificationClient notificationBailClient;

    private Cache<String, String> recentDeliveryReceiptCache;

    public BailGovNotifyNotificationSender(
        @Value("${notificationSender.deduplicateSendsWithinSeconds}") int deduplicateSendsWithinSeconds,
        RetryableNotificationClient notificationBailClient
    ) {
        this.deduplicateSendsWithinSeconds = deduplicateSendsWithinSeconds;
        this.notificationBailClient = notificationBailClient;
    }

    public synchronized String sendEmail(
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        String reference,
        CaseType caseType
    ) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache();
        return recentDeliveryReceiptCache.get(
            emailAddress + reference,
            k -> {

                try {

                    LOG.info("Attempting to send email notification to GovNotify: {}", reference);

                    SendEmailResponse response =
                            notificationBailClient.sendEmail(
                                    templateId,
                                    emailAddress,
                                    personalisation,
                                    reference
                            );

                    String notificationId =
                        response
                            .getNotificationId()
                            .toString();

                    LOG.info(
                        "Successfully sent email notification to GovNotify: {} ({})",
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

    @Override
    public synchronized String sendSms(
        final String templateId,
        final String phoneNumber,
        final Map<String, String> personalisation,
        final String reference,
        CaseType caseType) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache();
        return recentDeliveryReceiptCache.get(
            phoneNumber + reference,
            k -> {

                try {

                    LOG.info("Attempting to send a text message notification to GovNotify: {}", reference);

                    SendSmsResponse response =
                            notificationBailClient.sendSms(
                                    templateId,
                                    phoneNumber,
                                    personalisation,
                                    reference
                            );

                    String notificationId =
                        response
                            .getNotificationId()
                            .toString();

                    LOG.info(
                        "Successfully sent sms notification to GovNotify: {} ({})",
                        reference,
                        notificationId
                    );

                    return notificationId;

                } catch (NotificationClientException e) {
                    throw new NotificationServiceResponseException("Failed to send sms using GovNotify", e);
                }
            }
        );
    }

    private Cache<String, String> getOrCreateDeliveryReceiptCache() {

        if (recentDeliveryReceiptCache == null) {
            recentDeliveryReceiptCache =
                Caffeine
                    .newBuilder()
                    .expireAfterWrite(deduplicateSendsWithinSeconds, TimeUnit.SECONDS)
                    .build();
        }

        return recentDeliveryReceiptCache;
    }
}
