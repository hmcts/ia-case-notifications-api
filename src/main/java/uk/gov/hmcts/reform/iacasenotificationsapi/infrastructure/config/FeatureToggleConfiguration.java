package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import com.launchdarkly.client.LDClient;
import com.launchdarkly.client.LDConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfiguration {

    @Value("${launch-darkly-sdk-key}")
    private String sdkKey;

    @Bean
    public LDConfig ldConfig() {
        return new LDConfig.Builder()
            .connectTimeout(3)
            .socketTimeout(3)
            .build();
    }

    @Bean
    public LDClient ldClient(LDConfig ldConfig) {
        return new LDClient(sdkKey, ldConfig);
    }

}
