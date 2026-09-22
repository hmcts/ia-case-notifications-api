package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static tools.jackson.databind.cfg.EnumFeature.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder
            .featuresToEnable(READ_ENUMS_USING_TO_STRING)
            .featuresToEnable(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
            .featuresToEnable(WRITE_ENUMS_USING_TO_STRING)
            .serializationInclusion(JsonInclude.Include.NON_ABSENT)
            .createXmlMapper(false)
            .build();
    }
}
