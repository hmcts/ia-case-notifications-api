package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.health;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.HealthCheckConfiguration;


@ExtendWith(MockitoExtension.class)
public class DownStreamHealthIndicatorTest {

    @Mock
    RestTemplate restTemplate;
    @Mock
    HealthCheckConfiguration healthCheckConfiguration;

    @Test
    public void testGetContributor() {
        Map<String, Map<String, String>> services = new HashMap<>();
        services.put("service1", ImmutableMap.of("uri", "http://service1uri", "response", "\"status\":\"UP\""));
        services.put("service2", ImmutableMap.of("uri", "http://service2uri", "response", "\"status\":\"UP\""));

        when(healthCheckConfiguration.getServices()).thenReturn(services);

        DownStreamHealthIndicator downStreamHealthIndicator = new DownStreamHealthIndicator(restTemplate, healthCheckConfiguration);

        assertNotNull(downStreamHealthIndicator.getContributor("service2"));
        assertEquals(ServiceHealthIndicator.class, downStreamHealthIndicator.getContributor("service2").getClass());
    }

    @Test
    public void should_throw_exception_when_services_list_is_null_or_empty() {
        when(healthCheckConfiguration.getServices()).thenReturn(null);

        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> new DownStreamHealthIndicator(restTemplate, healthCheckConfiguration));
        assertEquals("HealthCheckConfiguration cannot be null or empty", exception.getMessage());
    }

}
