package uk.gov.hmcts.reform.iacasenotificationsapi.config;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;

@Configuration
public class DocumentUploadClientApiConfiguration {

    @Bean
    @Primary
    public DocumentUploadClientApi documentUploadClientApi(
        @Value("${ccdGatewayUrl}") final String ccdGatewayUrl
    ) {
        return new DocumentUploadClientApi(
            ccdGatewayUrl,
            new RestTemplate(),
            new JsonMapper()
        );
    }
}
