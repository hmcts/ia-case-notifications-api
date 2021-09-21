package uk.gov.hmcts.reform.iacasenotificationsapi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.openqa.grid.web.servlet.LifecycleServlet;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@SuppressWarnings("unchecked")
public final class MapValueExtractor {

    private MapValueExtractor() {
        // noop
    }

    public static <T> T extract(Map<String, Object> map, String path) {

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

            if (map1.containsKey("notificationsSent")) {
                List<Map<String, Object>> notificationsSent = (List<Map<String, Object>>) map1.get("notificationsSent");
                List<Map<String, Object>> updatedNotificationsSent = new ArrayList<>(notificationsSent);

                for (Map<String, Object> notificationSent : notificationsSent) {
                    String notificationValue = (String) notificationSent.get("id");

                    if (notificationValue.contains("_CASE_OFFICER")) {
                        updatedNotificationsSent.remove(notificationSent);
                    }
                }
                ((Map<String, Object>) value).remove("notificationsSent");
                ((Map<String, Object>) value).put("notificationsSent", updatedNotificationsSent);
            }

            //if (map1.containsKey("notifications")) {
            //    List<Map<String, Object>> notifications = (List<Map<String, Object>>) map1.get("notifications");
            //    List<Map<String, Object>> updatedNotifications = new ArrayList<>(notifications);
            //
            //    for (Map<String, Object> notification : notifications) {
            //        String notificationValue = (String) notification.get("reference");
            //
            //        if (notificationValue.contains("_CASE_OFFICER")) {
            //            updatedNotifications.remove(notification);
            //        }
            //    }
            //    ((Map<String, Object>) value).remove("notifications");
            //    ((Map<String, Object>) value).put("notifications", updatedNotifications);
            //}


            currentMap = (Map<String, Object>) value;

            System.out.println("currentMap =>> " + currentMap);
        }

        return (T) currentMap.get(pathParts[pathParts.length - 1]);
    }

    public static <T> T extractOrDefault(Map<String, Object> map, String path, T defaultValue) {

        T value = extract(map, path);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    public static <T> T extractOrThrow(Map<String, Object> map, String path) {

        T value = extract(map, path);

        if (value == null) {
            throw new RuntimeException("Missing value for path: " + path);
        }

        return value;
    }
}
