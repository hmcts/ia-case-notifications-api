package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.notify.CustomNotificationClient;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.notify.NotificationClientApi;

@Slf4j
@Configuration
public class GovNotifyConfiguration {

    @Bean
    @Primary
    public NotificationClientApi notificationClient(
        @Value("${govnotify.key}") String key,
        @Value("${govnotify.baseUrl}") String goveNotifyBAseUrl
    ) {
        requireNonNull(key);

        return new CustomNotificationClient(key, goveNotifyBAseUrl);
    }
}
