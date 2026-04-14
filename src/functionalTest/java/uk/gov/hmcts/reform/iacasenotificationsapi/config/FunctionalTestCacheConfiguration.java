package uk.gov.hmcts.reform.iacasenotificationsapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(FunctionalTestCacheConfiguration.class);

    @Bean
    @Primary
    public CacheManager cacheManagerCustomizer() {
        log.info("Caffeine cache manager..");
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
                "systemUserTokenCache"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(3600))
        );
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
}
