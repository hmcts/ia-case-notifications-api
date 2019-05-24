package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.service.notify.NotificationClient;

@Slf4j
@Configuration
public class GovNotifyConfiguration {

    @Bean
    @Primary
    public NotificationClient notificationClient(
        @Value("${govnotify.key}") String key
    ) {
        log.info("\n\n>>> Setting Bean value to {} \n\n", key);
        requireNonNull(key);
        return new NotificationClient(key);
    }
}
