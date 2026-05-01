package uk.gov.hmcts.reform.iacasenotificationsapi.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public final class MapFieldAssertor {

    private MapFieldAssertor() {
        // noop
    }

    public static void assertFields(
        Map<String, Object> expectedMap,
        Map<String, Object> actualMap
    ) {
        assertFields(expectedMap, actualMap, "");
    }

    public static void assertFields(
        Map<String, Object> expectedMap,
        Map<String, Object> actualMap,
        final String path
    ) {
        for (Map.Entry<String, Object> expectedEntry : expectedMap.entrySet()) {

            String key = expectedEntry.getKey();
            String pathWithKey = path + "." + key;

            Object expectedValue = expectedEntry.getValue();
            Object actualValue = actualMap.get(key);

            if ((expectedValue instanceof List expectedValueCollection) && (actualValue instanceof List actualValueCollection)) {
                if (key.equals("notificationsSent")) {
                    assertNotificationsSent(
                        expectedValueCollection,
                        actualValueCollection,
                        pathWithKey
                    );
                } else {
                    assertListInOrder(expectedValueCollection, actualValueCollection, pathWithKey);
                }
            } else {
                assertValue(expectedValue, actualValue, pathWithKey);
            }
        }
    }

    private static String idFrom(Map<String, Object> object) {
        return fieldFrom(object, "id");
    }

    private static String valueFrom(Map<String, Object> object) {
        return fieldFrom(object, "value");
    }

    private static String fieldFrom(Map<String, Object> object, String field) {
        return object.getOrDefault(field, "noValue").toString();
    }

    private static String notificationSentToReadableString(List<Map<String, Object>> collection) {
        return collection.stream()
            .map(MapFieldAssertor::idFrom)
            .map(MapFieldAssertor::removeTimestampFromNotificationReference)
            .collect(Collectors.joining("\n"));
    }

    private static void assertNotificationsSent(
        List<Map<String, Object>> expectedValueCollection,
        List<Map<String, Object>> actualValueCollection,
        String path
    ) {
        assertEquals(expectedValueCollection.size(), actualValueCollection.size(),
            "Expected and actual notificationSent collection size differ. Expected:\n"
                + notificationSentToReadableString(expectedValueCollection) + "\nActual:\n"
                + notificationSentToReadableString(actualValueCollection));
        for (Map<String, Object> expectedValueItem : expectedValueCollection) {
            Optional<Map<String, Object>> actualValueItem = Optional.empty();
            if (expectedValueItem != null) {
                actualValueItem = actualValueCollection.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> idFrom(item).contains(idFrom(expectedValueItem)))
                    .findFirst();
            }
            if (actualValueItem.isPresent()) {
                assertEquals(idFrom(expectedValueItem), removeTimestampFromNotificationReference(idFrom(actualValueItem.get())));
                String expectedValueString = valueFrom(expectedValueItem);
                if (isRegex(expectedValueString)) {
                    assertRegexMatches(expectedValueString, valueFrom(actualValueItem.get()), path);
                }
            } else {
                throw new AssertionError(
                    "Expected item: " + expectedValueItem + " not found in actual collection (" + path + "): " + actualValueCollection
                );
            }
        }
    }

    private static boolean isRegex(String value) {
        return value.length() > 3 && value.startsWith("$/") && value.endsWith("/");
    }

    private static void assertRegexMatches(
        String expectedValueRegex,
        String actualValue,
        String path
    ) {
        expectedValueRegex = expectedValueRegex.substring(2, expectedValueRegex.length() - 1);
        assertThat(
            "Expected field matches regular expression (" + path + ")",
            actualValue,
            matchesPattern(expectedValueRegex)
        );
    }

    private static void assertListInOrder(
        List expectedValueCollection,
        List actualValueCollection,
        String pathWithKey
    ) {
        for (int i = 0; i < expectedValueCollection.size(); i++) {

            String pathWithKeyAndIndex = pathWithKey + "." + i;

            Object expectedValueItem = expectedValueCollection.get(i);
            Object actualValueItem =
                i < actualValueCollection.size()
                    ? actualValueCollection.get(i)
                    : null;

            assertValue(expectedValueItem, actualValueItem, pathWithKeyAndIndex);
        }
    }

    private static void assertValue(
        Object expectedValue,
        Object actualValue,
        String path
    ) {
        if ((expectedValue instanceof Map) && (actualValue instanceof Map)) {

            assertFields(
                (Map<String, Object>) expectedValue,
                (Map<String, Object>) actualValue,
                path
            );

        } else {

            if ((expectedValue instanceof String expectedValueString) && (actualValue instanceof String)) {

                if (isRegex(expectedValueString)) {
                    assertRegexMatches(expectedValueString, (String) actualValue, path);
                    return;
                }

                if (expectedValueString.startsWith("contains([")) {

                    Stream
                        .of(expectedValueString.substring(10, expectedValueString.length() - 2)
                            .split(","))
                        .forEach(expectedValueItem -> {
                            assertThat(
                                "Expected field contains (" + path + ")",
                                String.valueOf(actualValue),
                                containsString(expectedValueItem)
                            );
                        });
                    return;
                }
            }

            assertThat(
                "Expected field matches (" + path + ")",
                actualValue,
                equalTo(expectedValue)
            );
        }
    }

    public static String removeTimestampFromNotificationReference(String input) {
        return input.replaceAll("_(\\d{13})$", "");
    }

}
