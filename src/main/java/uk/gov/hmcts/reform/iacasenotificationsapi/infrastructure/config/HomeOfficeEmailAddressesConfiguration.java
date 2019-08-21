package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;

@Configuration
@ConfigurationProperties
public class HomeOfficeEmailAddressesConfiguration {

    private Map<HearingCentre, String> homeOfficeEmailAddresses = new EnumMap<>(HearingCentre.class);

    public Map<HearingCentre, String> getHomeOfficeEmailAddresses() {
        return homeOfficeEmailAddresses;
    }

    @Bean
    public Map<HearingCentre, String> homeOfficeEmailAddresses() {
        return homeOfficeEmailAddresses;
    }
}
