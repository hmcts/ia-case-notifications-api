package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "mappings")
@Data
public class DetentionFacilityNameMappingConfig {
    private List<KeyValueEntry> prisonNames;
    private List<KeyValueEntry> ircNames;

    @Data
    public static class KeyValueEntry {
        private String key;
        private String value;
    }

    public Map<String, String> getPrisonNamesMapping() {
        Map<String, String> result = new HashMap<>();
        for (KeyValueEntry entry : prisonNames) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public Map<String, String> getIrcNamesMapping() {
        Map<String, String> result = new HashMap<>();
        for (KeyValueEntry entry : ircNames) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
