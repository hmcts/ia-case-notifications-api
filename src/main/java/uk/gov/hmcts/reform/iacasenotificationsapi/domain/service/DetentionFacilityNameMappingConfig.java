package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "mappings")
@Data
public class DetentionFacilityNameMappingConfig {
    private List<NameMapping> prisonNames;
    private List<NameMapping> ircNames;

    public Map<String, String> getPrisonNamesMapping() {
        Map<String, String> result = new HashMap<>();
        for (NameMapping entry : prisonNames) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public Map<String, String> getIrcNamesMapping() {
        Map<String, String> result = new HashMap<>();
        for (NameMapping entry : ircNames) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Data
    public static class NameMapping {
        private String key;
        private String value;
    }
}
