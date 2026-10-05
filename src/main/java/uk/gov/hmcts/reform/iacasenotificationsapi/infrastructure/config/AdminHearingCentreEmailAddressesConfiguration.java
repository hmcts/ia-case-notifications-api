package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@Configuration
@ConfigurationProperties
public class AdminHearingCentreEmailAddressesConfiguration {

    private Map<HearingCentre, String> adminHearingCentreEmailAddresses = new EnumMap<>(HearingCentre.class);

    public Map<HearingCentre, String> getAdminHearingCentreEmailAddresses() {
        return adminHearingCentreEmailAddresses;
    }

    @Bean
    public Map<HearingCentre, String> adminHearingCentreEmailAddresses() {
        return Collections.unmodifiableMap(adminHearingCentreEmailAddresses);
    }

}
