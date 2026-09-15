package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

public interface WithDocumentDownloadStub {

    default void addDocumentDownloadStub(WireMockServer server) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlMatching("/.*/binary"))
                    .build(),
                aResponse()
                    .withStatus(201)
                    .withBody("{\n"
                        + "  \"id\" : \"" + "" + "\",\n"
                        + "  \"content\": {\n"
                        + "    \"body\" : \"some-body\",\n"
                        + "    \"subject\" : \"some-subject\"\n"
                        + "  },\n"
                        + "  \"template\": {\n"
                        + "    \"id\" : \"" + "" + "\",\n"
                        + "    \"version\" : 1,\n"
                        + "    \"uri\" : \"some-uri\"\n"
                        + "  }\n"
                        + "}")
                    .build()));
    }
}
