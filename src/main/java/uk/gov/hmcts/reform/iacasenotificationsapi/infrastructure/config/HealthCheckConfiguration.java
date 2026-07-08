package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "health")
public class HealthCheckConfiguration {

    private Map<String, Map<String, String>> services = new HashMap<>();

    public Map<String, Map<String, String>> getServices() {
        return services.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> Collections.unmodifiableMap(e.getValue())
            ));
    }

    public void setServices(Map<String, Map<String, String>> services) {
        this.services = services;
    }
}
