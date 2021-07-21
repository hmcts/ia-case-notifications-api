package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
        @Value("${govnotify.key}") String key,
        @Value("${govnotify.baseUrl}") String goveNotifyBAseUrl,
        @Value("${govnotify.timeout}") int timeout
    ) {
        requireNonNull(key);

        return new NotificationClient(key, goveNotifyBAseUrl) {

            HttpURLConnection getConnection(URL url) throws IOException {

                log.info("creating connection for url");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);

                return conn;
            }

        };
    }
}
