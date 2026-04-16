package uk.gov.hmcts.reform.iacasenotificationsapi.verifiers;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Strings;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.RetryableNotificationClient;
import uk.gov.hmcts.reform.iacasenotificationsapi.util.MapValueExtractor;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClientException;

@Component
@SuppressWarnings("unchecked")
public class NotificationVerifier implements Verifier {

    @Autowired
    private RetryableNotificationClient notificationClient;

    public void verify(
        long testCaseId,
        Map<String, Object> scenario,
        Map<String, Object> expectedResponse,
        Map<String, Object> actualResponse
    ) {
        String description = MapValueExtractor.extract(scenario, "description");

        List<Map<String, Object>> expectedNotifications =
            MapValueExtractor.extractOrDefault(scenario, "expectation.notifications", Collections.emptyList());

        if (expectedNotifications == null || expectedNotifications.isEmpty()) {
            return;
        }

        List<Map<String, Object>> notificationsSent =
            MapValueExtractor.extractOrDefault(actualResponse, "data.notificationsSent", Collections.emptyList());

        if (notificationsSent.isEmpty()) {
            assertFalse(
                notificationsSent.isEmpty(),
                description + ": Notifications were not delivered"
            );
        }

        Map<String, String> notificationsSentMap =
            notificationsSent
                .stream()
                .collect(Collectors.toMap(
                    notificationSent -> sanitizeNotificationId((String) notificationSent.get("id")),
                    notificationSent -> (String) notificationSent.get("value")
                ));

        expectedNotifications
            .forEach(expectedNotification -> {

                final String expectedReference = MapValueExtractor.extractOrThrow(expectedNotification, "reference");
                final String expectedRecipient = MapValueExtractor.extractOrThrow(expectedNotification, "recipient");
                final String expectedSubject = MapValueExtractor.extractOrThrow(expectedNotification, "subject");
                final Object expectedBodyUnknownType = MapValueExtractor.extractOrThrow(expectedNotification, "body");

                final String deliveredNotificationId = notificationsSentMap.get(expectedReference);
                if (Strings.isNullOrEmpty(deliveredNotificationId)) {
                    assertFalse(
                        true,
                        description
                            + ": Notification " + expectedReference + " was not delivered successfully"
                    );
                }

                try {

                    Notification notification = notificationClient.getNotificationById(deliveredNotificationId);

                    final String actualReference = sanitizeNotificationId(notification.getReference().orElse(""));
                    final String actualRecipient =
                        notification.getEmailAddress().orElse(notification.getPhoneNumber().orElse(""));
                    final String actualSubject = notification.getSubject().orElse("");
                    final String actualBody = notification.getBody();

                    assertEquals(
                        expectedReference,
                        actualReference,
                        description + ": Notification "
                            + expectedReference + " was delivered with wrong reference"
                    );

                    assertEquals(
                        expectedRecipient,
                        actualRecipient,
                        description + ": Notification "
                            + expectedReference + " was delivered to wrong recipient"
                    );

                    assertEquals(
                        expectedSubject,
                        actualSubject,
                        description + ": Notification "
                            + expectedReference + " was delivered with wrong subject content"
                    );

                    if (expectedBodyUnknownType instanceof String) {

                        assertEquals(
                            expectedBodyUnknownType,
                            actualBody,
                            description + ": Notification "
                                + expectedReference + " was delivered with wrong body content"
                        );

                    } else {

                        List<String> expectedBodyMatches = (List<String>) expectedBodyUnknownType;

                        expectedBodyMatches.forEach(expectedBodyMatch -> {

                            assertTrue(
                                actualBody.contains(expectedBodyMatch),
                                description + ": Notification "
                                    + expectedReference + " was delivered with wrong body content match"
                            );
                        });
                    }

                } catch (NotificationClientException e) {
                    assertFalse(
                        true,
                        description + ": Notification " + deliveredNotificationId + " was not found on GovNotify"
                    );
                }
            });
    }

    private String sanitizeNotificationId(String notificationIdWithTimestamp) {
        // Regular expression to remove the last underscore and following timestamp in epochmillis
        Pattern pattern = Pattern.compile("^(.*)_\\d{13}$");
        Matcher matcher = pattern.matcher(notificationIdWithTimestamp);
        return matcher.find() ? matcher.group(1) : notificationIdWithTimestamp;
    }
}
