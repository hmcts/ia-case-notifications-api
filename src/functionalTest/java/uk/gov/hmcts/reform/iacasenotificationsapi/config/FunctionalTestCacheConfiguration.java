package uk.gov.hmcts.reform.iacasenotificationsapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
@EnableCaching
@Profile("functional")
public class FunctionalTestCacheConfiguration {

    @Bean
    @Primary
    public CacheManager cacheManagerCustomizer() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "userInfoCache",
                "legalRepATokenCache",
                "caseOfficerTokenCache",
                "adminOfficerTokenCache",
                "homeOfficeApcTokenCache",
                "homeOfficeLartTokenCache",
                "homeOfficePouTokenCache",
                "homeOfficeGenericTokenCache",
                "legalRepShareCaseATokenCache",
                "judgeTokenCache",
                "citizenTokenCache",
                "systemTokenCache"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(3600))
        );
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
