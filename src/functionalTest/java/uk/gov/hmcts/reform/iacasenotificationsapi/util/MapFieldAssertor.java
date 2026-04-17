package uk.gov.hmcts.reform.iacasenotificationsapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
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

                for (int i = 0; i < expectedValueCollection.size(); i++) {

                    String pathWithKeyAndIndex = pathWithKey + "." + i;

                    Object expectedValueItem = expectedValueCollection.get(i);
                    Object actualValueItem =
                        i < actualValueCollection.size()
                            ? actualValueCollection.get(i)
                            : null;

                    assertValue(expectedValueItem, actualValueItem, pathWithKeyAndIndex);
                }

            } else {
                assertValue(expectedValue, actualValue, pathWithKey);
            }
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

            if ((expectedValue instanceof String expectedValueString) && (actualValue instanceof String actualValueString)) {

                if (isPathContainsNotificationsSentReference(path)) {
                    assertEquals(expectedValue, removeTimestampFromNotificationReference(actualValueString),
                        "Expected field matches (" + path + ")");
                    return;
                }

                if (expectedValueString.length() > 3
                    && expectedValueString.startsWith("$/")
                    && expectedValueString.endsWith("/")) {

                    expectedValueString = expectedValueString.substring(2, expectedValueString.length() - 1);

                    assertEquals(
                        expectedValueString,
                        actualValueString,
                        "Expected field matches regular expression (" + path + ")"
                    );

                    return;
                }

                if (expectedValueString.startsWith("contains([")) {

                    Stream
                        .of(expectedValueString.substring(10, expectedValueString.length() - 2)
                            .split(","))
                        .forEach(expectedValueItem -> {
                            assertEquals(
                                expectedValueItem,
                                String.valueOf(actualValue),
                                "Expected field contains (" + path + ")"
                            );
                        });
                    return;
                }
            }

            assertEquals(
                expectedValue,
                actualValue,
                "Expected field matches (" + path + ")"
            );
        }
    }

    private static boolean isPathContainsNotificationsSentReference(String path) {
        // Regular expression to match the notificationsSent id format
        String regex = ".*data\\.notificationsSent\\.\\d+\\.id.*";

        // Check if the input matches the pattern
        return path.matches(regex);
    }

    public static String removeTimestampFromNotificationReference(String input) {
        return input.replaceAll("_(\\d{13})$", "");
    }

}
