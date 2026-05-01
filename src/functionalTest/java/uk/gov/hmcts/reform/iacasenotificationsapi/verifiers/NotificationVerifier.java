package uk.gov.hmcts.reform.iacasenotificationsapi.verifiers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
    @Autowired
    @Qualifier("BailClient")
    private RetryableNotificationClient notificationBailClient;

    @Value("${iaExUiFrontendUrl}")
    private String iaExUiFrontendUrl;

    @Value("${iaAipFrontendUrl}")
    private String iaAipFrontendUrl;

    public void verify(
        String fileName,
        long testCaseId,
        Map<String, Object> scenario,
        Map<String, Object> expectedResponse,
        Map<String, Object> actualResponse
    ) {
        List<Map<String, Object>> expectedNotifications =
            MapValueExtractor.extractOrDefault(scenario, "expectation.notifications", Collections.emptyList());

        if (expectedNotifications == null || expectedNotifications.isEmpty()) {
            return;
        }

        List<Map<String, Object>> notificationsSent;
        final String requestUri = MapValueExtractor.extractOrThrow(scenario, "request.uri");
        boolean isBail = requestUri.contains("bail");
        if (requestUri.contains("ccdSubmitted")) {
            Pattern notificationsSentPattern = Pattern.compile("notificationsSent=\\[[^]]*]");

            String confirmationBody = MapValueExtractor.extractOrThrow(actualResponse, "confirmation_body");
            Matcher notificationsSentMatcher = notificationsSentPattern.matcher(confirmationBody);
            String notificationsSentString;
            if (notificationsSentMatcher.find()) {
                notificationsSentString = notificationsSentMatcher.group();
            } else {
                throw new RuntimeException(fileName + ": notificationsSent was not found in confirmation body of actual response");
            }

            Pattern pattern = Pattern.compile("IdValue\\(id=([A-Za-z0-9_-]*), value=([A-Za-z0-9-]*)\\)");
            Matcher matcher = pattern.matcher(notificationsSentString);
            notificationsSent = new ArrayList<>();

            while (matcher.find()) {
                String id = matcher.group(1).trim();
                String value = matcher.group(2).trim();
                notificationsSent.add(Map.of("id", id, "value", value));
            }
        } else {
            notificationsSent =
                MapValueExtractor.extractOrDefault(actualResponse, "data.notificationsSent", Collections.emptyList());
        }
        assertFalse(
            notificationsSent.isEmpty(),
            fileName + ": Notifications were not found in notificationsSent of actual response"
        );

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
                final Object expectedBodyExclusions = MapValueExtractor.extractOrDefault(expectedNotification, "bodyExclusions", Collections.emptyList());

                final String deliveredNotificationId = notificationsSentMap.get(expectedReference);
                assertFalse(
                    Strings.isNullOrEmpty(deliveredNotificationId),
                    fileName
                        + ": Notification " + expectedReference + " was not delivered successfully"
                );
                for (int attempt = 1; attempt <= 3; attempt++) {
                    try {

                        Notification notification = isBail ? notificationBailClient.getNotificationById(deliveredNotificationId)
                            : notificationClient.getNotificationById(deliveredNotificationId);

                        final String actualReference = sanitizeNotificationId(notification.getReference().orElse(""));
                        final String actualRecipient =
                            notification.getEmailAddress().orElse(notification.getPhoneNumber().orElse(""));
                        final String actualSubject = notification.getSubject().orElse("");
                        final String actualBody = notification.getBody();

                        assertEquals(
                            expectedReference,
                            actualReference,
                            fileName + ": Notification "
                                + expectedReference + " was delivered with wrong reference"
                        );

                        assertEquals(
                            expectedRecipient,
                            actualRecipient,
                            fileName + ": Notification "
                                + expectedReference + " was delivered to wrong recipient"
                        );

                        assertEquals(
                            expectedSubject,
                            actualSubject,
                            fileName + ": Notification "
                                + expectedReference + " was delivered with wrong subject content"
                        );

                        if (expectedBodyUnknownType instanceof String) {

                            assertEquals(
                                expectedBodyUnknownType,
                                actualBody,
                                fileName + ": Notification "
                                    + expectedReference + " was delivered with wrong body content"
                            );

                        } else {

                            List<String> expectedBodyMatches = (List<String>) expectedBodyUnknownType;

                            expectedBodyMatches.forEach(expectedBodyMatch -> assertTrue(
                                    getVerification(expectedBodyMatch, actualBody),
                                    "Notification "
                                        + expectedReference + " was delivered with wrong body content. Expected body to contain: "
                                        + expectedBodyMatch + " but was: " + actualBody
                                )
                            );
                        }

                        for (String expectedBodyExclusion : (List<String>) expectedBodyExclusions) {
                            assertFalse(
                                actualBody.contains(expectedBodyExclusion),
                                "Notification "
                                    + expectedReference + " was delivered with wrong body content. Expected body to not contain: "
                                    + expectedBodyExclusion + " but was: " + actualBody
                            );
                        }

                    } catch (NotificationClientException e) {
                        if (attempt == 3) {
                            fail(fileName + ": Notification " + deliveredNotificationId + " was not found on GovNotify after 3 attempts");
                        }
                    }
                }
            });
    }

    private boolean getVerification(String expectedBodyMatch, String actualBody) {
        boolean verification = actualBody.contains(expectedBodyMatch);
        if (expectedBodyMatch.equals(iaExUiFrontendUrl)) {
            verification = verification || (actualBody.contains("https://manage-case") && actualBody.contains("platform.hmcts.net"));
        } else if (expectedBodyMatch.equals(iaAipFrontendUrl)) {
            verification = verification || actualBody.contains("https://www.appeal-immigration-asylum-decision.service.gov.uk/");
        }
        return verification;
    }

    private String sanitizeNotificationId(String notificationIdWithTimestamp) {
        // Regular expression to remove the last underscore and following timestamp in epochmillis
        Pattern pattern = Pattern.compile("^(.*)_\\d{13}$");
        Matcher matcher = pattern.matcher(notificationIdWithTimestamp);
        return matcher.find() ? matcher.group(1) : notificationIdWithTimestamp;
    }
}
