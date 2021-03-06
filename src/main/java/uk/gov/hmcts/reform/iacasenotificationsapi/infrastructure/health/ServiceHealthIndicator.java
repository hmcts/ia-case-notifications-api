package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class ServiceHealthIndicator implements HealthIndicator {

    private String uri;
    private RestTemplate restTemplate;

    public ServiceHealthIndicator(String uri, String matcher, RestTemplate restTemplate) {
        this.uri = uri;
        this.restTemplate = restTemplate;
    }


    @Override
    public Health health() {

        try {
            ResponseEntity<String> response = restTemplate
                .getForEntity(uri, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return new Health
                    .Builder(Status.UP)
                    .build();
            } else {

                return new Health
                    .Builder(Status.DOWN)
                    .build();
            }
        } catch (RestClientException ex) {

            log.error("Downstream service exception {}", Health.down(ex).build().getDetails());
            return new Health
                .Builder()
                .down(ex)
                .build();
        }
    }
}
