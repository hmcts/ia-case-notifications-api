package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.Builder;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.UserDetailsForTest;

@SuppressWarnings("OperatorWrap")
public class GivensBuilder {

    private final Resource resourceJwksFile;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GivensBuilder(Resource resourceJwksFile) {
        this.resourceJwksFile = resourceJwksFile;
    }

    public GivensBuilder someLoggedIn(UserDetailsForTest.UserDetailsForTestBuilder userDetailsForTestBuilder) {

        String jwksResponse = "";
        try {
            jwksResponse = FileUtils.readFileToString(resourceJwksFile.getFile());
        } catch (IOException e) {
            // ignore this
        }

        stubFor(get(urlEqualTo("/userAuth/o/jwks"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jwksResponse)));

        stubFor(get(urlEqualTo("/userAuth/o/userinfo"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    getObjectAsJsonString(
                        userDetailsForTestBuilder))));

        return this;
    }

    public GivensBuilder govNotifyWillHandleEmailNotificationAndReturnNotificationId(String someNotificationId) {

        stubFor(post(urlEqualTo("/v2/notifications/email"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"id\" : \"" + someNotificationId +  "\",\n" +
                    "  \"content\": {\n" +
                    "    \"body\" : \"some-body\",\n" +
                    "    \"subject\" : \"some-subject\"\n" +
                    "  },\n" +
                    "  \"template\": {\n" +
                    "    \"id\" : \""  + UUID.randomUUID().toString() + "\",\n" +
                    "    \"version\" : 1,\n" +
                    "    \"uri\" : \"some-uri\"\n" +
                    "  }\n" +
                    "}")));

        return this;
    }

    private String getObjectAsJsonString(Builder builder) {

        try {
            return objectMapper.writeValueAsString(builder.build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't serialize object", e);
        }
    }

    public GivensBuilder and() {
        return this;
    }
}
