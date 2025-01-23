package uk.gov.hmcts.reform.iacasenotificationsapi.verifiers;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.RetryableNotificationClient;
import uk.gov.hmcts.reform.iacasenotificationsapi.util.MapValueExtractor;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClientException;

@Component
@SuppressWarnings("unchecked")
@Slf4j
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

        if (expectedNotifications == null
            || expectedNotifications.size() == 0) {
            return;
        }

        List<Map<String, Object>> notificationsSentWithTimestamp =
            MapValueExtractor.extractOrDefault(actualResponse, "data.notificationsSent", Collections.emptyList());
        List<Map<String, Object>> notificationsSent = new ArrayList<>();
        sanitizeNotifications(notificationsSentWithTimestamp, notificationsSent);

        // Print the processed list
        for (Map<String, Object> map : notificationsSent) {
            map.forEach((key, value) ->
                    log.info("Notifications sent for testCaseId: {}, key->value : {}->{}", testCaseId, key, value));
        }

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
                    notificationSent -> (String) notificationSent.get("id"),
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

                    final String actualReference = notification.getReference().orElse("");
                    final String actualRecipient =
                        notification.getEmailAddress().orElse(notification.getPhoneNumber().orElse(""));
                    final String actualSubject = notification.getSubject().orElse("");
                    final String actualBody = notification.getBody();

                    assertThat(
                        description
                            + ": Notification "
                            + expectedReference
                            + " was delivered with wrong reference",
                        actualReference,
                        equalTo(expectedReference)
                    );

                    assertThat(
                        description
                            + ": Notification "
                            + expectedReference
                            + " was delivered to wrong recipient",
                        actualRecipient,
                        equalTo(expectedRecipient)
                    );

                    assertThat(
                        description
                            + ": Notification "
                            + expectedReference
                            + " was delivered with wrong subject content",
                        actualSubject,
                        equalTo(expectedSubject)
                    );

                    if (expectedBodyUnknownType instanceof String) {

                        assertThat(
                            description
                                + ": Notification "
                                + expectedReference
                                + " was delivered with wrong body content",
                            actualBody,
                            equalTo((String) expectedBodyUnknownType)
                        );

                    } else {

                        List<String> expectedBodyMatches = (List<String>) expectedBodyUnknownType;

                        expectedBodyMatches.forEach(expectedBodyMatch -> {

                            assertThat(
                                description
                                    + ": Notification "
                                    + expectedReference
                                    + " was delivered with wrong body content match",
                                actualBody,
                                containsString(expectedBodyMatch)
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

    private static void sanitizeNotifications(List<Map<String, Object>> notificationsSentWithTimestamp,
                                              List<Map<String, Object>> notificationsSent) {

        for (Map<String, Object> map : notificationsSentWithTimestamp) {
            Map<String, Object> newMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();

                // Trim the key up to the last underscore to exclude timestamp
                int lastIndexOfUnderscore = key.lastIndexOf('_');
                String trimmedKey = (lastIndexOfUnderscore != -1) ? key.substring(0, lastIndexOfUnderscore) : key;

                newMap.put(trimmedKey, entry.getValue());
            }
            notificationsSent.add(newMap);
        }
    }
}
