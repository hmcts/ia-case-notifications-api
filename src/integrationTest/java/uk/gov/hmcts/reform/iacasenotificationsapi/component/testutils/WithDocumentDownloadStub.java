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
                    .withBody("""
                        {
                          "id" : "\
                        ",
                          "content": {
                            "body" : "some-body",
                            "subject" : "some-subject"
                          },
                          "template": {
                            "id" : "\
                        ",
                            "version" : 1,
                            "uri" : "some-uri"
                          }
                        }\
                        """)
                    .build()));
    }
}
