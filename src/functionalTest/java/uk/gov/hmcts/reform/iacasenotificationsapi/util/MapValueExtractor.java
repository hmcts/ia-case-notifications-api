package uk.gov.hmcts.reform.iacasenotificationsapi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public final class MapValueExtractor {

    private MapValueExtractor() {
        // noop
    }

    public static <T> T extract(Map<String, Object> map, String path, Boolean featureFlag) {

        if (path.equals("expectation")) {
            return (T) map.get(path);
        }

        if (!path.contains(".")) {
            return (T) map.get(path);
        }

        Map<String, Object> currentMap = map;

        String[] pathParts = path.split("\\.");

        for (int i = 0; i < pathParts.length - 1; i++) {

            Object value = currentMap.get(pathParts[i]);

            if (!(value instanceof Map)) {
                return null;
            }

            Map map1 = (Map) value;

            if (featureFlag == false) {

                if (map1.containsKey("body")) {

                    String body = (String) map1.get("body");

                    if (body.contains("contains([")) {

                        if (body.contains("_CASE_OFFICER")) {
                            final String sPattern = "(?i)\\b\\w*" + Pattern.quote("_CASE_OFFICER") + "\\w*\\b";
                            Pattern pattern = Pattern.compile(sPattern);
                            Matcher matcher = pattern.matcher(body);
                            String transformedBody = "";

                            while (matcher.find()) {
                                transformedBody = body.replace(matcher.group(), "");
                            }

                            ((Map<String, Object>) value).remove("body");
                            ((Map<String, Object>) value).put("body", transformedBody);
                        }
                    }
                }

                if (map1.containsKey("caseData")) {
                    Map<String, Object> caseData = (Map<String, Object>) map1.get("caseData");
                    Map<String, Object> updateCaseData = new HashMap<>(caseData);

                    if (caseData.containsKey("replacements")) {
                        Map<String, Object> replacement = (Map<String, Object>) caseData.get("replacements");
                        Map<String, Object> updatedReplacement = new HashMap<>(replacement);

                        if (replacement.containsKey("notificationsSent")) {
                            List<Map<String, Object>> notificationsSent = (List<Map<String, Object>>) replacement.get("notificationsSent");
                            List<Map<String, Object>> updatedNotificationsSent = new ArrayList<>(notificationsSent);

                            for (Map<String, Object> notificationSent : notificationsSent) {
                                String notificationValue = (String) notificationSent.get("id");

                                if (notificationValue.contains("_CASE_OFFICER")) {
                                    updatedNotificationsSent.remove(notificationSent);
                                }
                            }

                            replacement.remove("notificationsSent");

                            caseData.remove("replacements");
                            caseData.put("replacements", updatedReplacement);

                            ((Map<String, Object>) value).remove("caseData");
                            ((Map<String, Object>) value).put("caseData", updateCaseData);
                        }
                    }
                }

                if (map1.containsKey("notifications")) {
                    List<Map<String, Object>> notifications = (List<Map<String, Object>>) map1.get("notifications");
                    List<Map<String, Object>> updatedNotifications = new ArrayList<>(notifications);

                    for (Map<String, Object> notification : notifications) {
                        String notificationValue = (String) notification.get("reference");

                        if (notificationValue.contains("_CASE_OFFICER")) {
                            updatedNotifications.remove(notification);
                        }
                    }
                    ((Map<String, Object>) value).remove("notifications");
                    ((Map<String, Object>) value).put("notifications", updatedNotifications);
                }
            }
            currentMap = (Map<String, Object>) value;
        }

        return (T) currentMap.get(pathParts[pathParts.length - 1]);
    }


    public static <T> T extractOrDefault(Map<String, Object> map, String path, T defaultValue, Boolean featureFlag) {

        T value = extract(map, path, featureFlag);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public static <T> T extractOrThrow(Map<String, Object> map, String path) {

        T value = extract(map, path, false);

        if (value == null) {
            throw new RuntimeException("Missing value for path: " + path);
        }

        return value;
    }
}
