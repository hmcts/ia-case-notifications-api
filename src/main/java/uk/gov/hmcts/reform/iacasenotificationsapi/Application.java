package uk.gov.hmcts.reform.iacasenotificationsapi;

import com.launchdarkly.client.LDClient;
import java.io.IOException;
import javax.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableCircuitBreaker
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.reform.auth",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.iacasenotificationsapi",
    })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    private final LDClient ldClient;

    public Application(LDClient ldClient) {
        this.ldClient = ldClient;
    }

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PreDestroy
    public void onShutdown() throws IOException {
        ldClient.close();
    }
}

