package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import tools.jackson.databind.json.JsonMapper;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

@Configuration
public class FeignConfiguration {

    @Bean
    public HttpMessageConverter<?> feignJacksonHttpMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonHttpMessageConverter(jsonMapper);
    }

    @Bean
    public FeignHttpMessageConverters feignHttpMessageConverters(
            ObjectProvider<ClientHttpMessageConvertersCustomizer> messageConverters,
            ObjectProvider<HttpMessageConverterCustomizer> customizers
    ) {
        return new FeignHttpMessageConverters(messageConverters, customizers);
    }

    @Bean
    @Primary
    public Encoder feignFormEncoder(
            ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters
    ) {
        return new SpringFormEncoder(new SpringEncoder(feignHttpMessageConverters));
    }

    @Bean
    @Primary
    public Decoder decoder(ObjectProvider<FeignHttpMessageConverters> feignHttpMessageConverters) {
        return new ResponseEntityDecoder(new SpringDecoder(feignHttpMessageConverters));
    }
}