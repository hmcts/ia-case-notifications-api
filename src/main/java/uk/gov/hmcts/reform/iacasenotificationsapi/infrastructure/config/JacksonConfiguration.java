package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;

@Configuration
public class JacksonConfiguration {

    // Boot 4 auto-configures a JsonMapper.Builder (and, from it, the primary JsonMapper
    // bean) itself. This customizer is applied to that shared builder, so any
    // spring.jackson.* properties still take effect alongside these settings.
    @Bean
    @Primary
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> builder
                .configure(EnumFeature.READ_ENUMS_USING_TO_STRING, true)
                .configure(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
                .configure(EnumFeature.WRITE_ENUMS_USING_TO_STRING, true)
                .changeDefaultPropertyInclusion(inclusion ->
                        inclusion
                                .withValueInclusion(JsonInclude.Include.NON_ABSENT)
                                .withContentInclusion(JsonInclude.Include.NON_ABSENT));
    }
}
