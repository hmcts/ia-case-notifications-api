package uk.gov.hmcts.reform.iacasenotificationsapi.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.fixtures.DocumentManagementFilesFixture;

@Component
@SuppressWarnings("unchecked")
public final class MapValueExpander {

    private static final Pattern TODAY_PATTERN = Pattern.compile("\\{\\$TODAY([+-]?\\d*?)\\|?([^0-9]*?)}");
    private static final Pattern ENVIRONMENT_PROPERTY_PATTERN = Pattern.compile("\\{\\$([a-zA-Z0-9].+?)}");
    public static final Properties ENVIRONMENT_PROPERTIES = new Properties(System.getProperties());

    @Autowired
    private DocumentManagementFilesFixture documentManagementFilesFixture;


    private MapValueExpander() {
        // noop
    }

    public void expandValues(Map<String, Object> map) {

        for (Map.Entry<String, Object> entry : map.entrySet()) {

            Object untypedValue = entry.getValue();

            if (untypedValue instanceof List) {

                untypedValue =
                    ((List) untypedValue)
                        .stream()
                        .map(element -> this.expandValue(element))
                        .collect(Collectors.toList());

            } else {
                untypedValue = expandValue(untypedValue);
            }

            entry.setValue(untypedValue);
        }
    }

    private Object expandValue(Object untypedValue) {

        if (untypedValue instanceof Map) {

            expandValues((Map<String, Object>) untypedValue);

        } else if (untypedValue instanceof String) {

            String value = (String) untypedValue;

            if (TODAY_PATTERN.matcher(value).find()) {
                value = expandToday(value);
            }

            if (ENVIRONMENT_PROPERTY_PATTERN.matcher(value).find()) {
                value = expandEnvironmentProperty(value);
            }


            return value;
        }

        return untypedValue;
    }

    private String expandToday(String value) {

        Matcher matcher = TODAY_PATTERN.matcher(value);

        String expandedValue = value;

        while (matcher.find()) {

            char plusOrMinus = '+';
            int dayAdjustment = 0;

            if (matcher.groupCount() >= 1
                && !matcher.group(1).isEmpty()) {

                plusOrMinus = matcher.group(1).charAt(0);
                dayAdjustment = Integer.valueOf(matcher.group(1).substring(1));
            }

            LocalDate now = LocalDate.now();

            if (plusOrMinus == '+') {
                now = now.plusDays(dayAdjustment);
            } else {
                now = now.minusDays(dayAdjustment);
            }

            String dateFormat = "yyyy-MM-dd";

            if (matcher.groupCount() >= 2
                && !matcher.group(2).isEmpty()) {
                dateFormat = matcher.group(2);
            }

            String token = matcher.group(0);

            expandedValue = expandedValue.replace(
                token,
                now.format(DateTimeFormatter.ofPattern(dateFormat))
            );
        }

        return expandedValue;
    }

    private String expandEnvironmentProperty(String value) {

        Matcher matcher = ENVIRONMENT_PROPERTY_PATTERN.matcher(value);

        String expandedValue = value;

        while (matcher.find()) {

            if (matcher.groupCount() == 1
                && !matcher.group(1).isEmpty()) {

                String variableName = matcher.group(1);
                String token = matcher.group(0);
                if (documentManagementFilesFixture.getProperties().containsKey(variableName)) {
                    expandedValue = documentManagementFilesFixture.getProperties().get(variableName);

                } else {
                    expandedValue = expandedValue.replace(token, ENVIRONMENT_PROPERTIES.getProperty(variableName));
                }
            }
        }

        return expandedValue;
    }
}
