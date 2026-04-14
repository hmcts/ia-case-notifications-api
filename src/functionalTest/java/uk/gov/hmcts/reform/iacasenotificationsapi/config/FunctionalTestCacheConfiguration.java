package uk.gov.hmcts.reform.iacasenotificationsapi.config;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableCaching
@Profile("functional")
public class FunctionalTestCacheConfiguration {

    @Bean
    @Primary
    public CacheManagerCustomizer<CaffeineCacheManager> cacheManagerCustomizer() {
        return cacheManager -> cacheManager.setAllowNullValues(false);
    }
}
