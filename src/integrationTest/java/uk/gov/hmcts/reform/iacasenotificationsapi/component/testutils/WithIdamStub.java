package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

public interface WithIdamStub {
    public static final String SERVICE_JWT_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3O"
        + "DkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJS"
        + "MeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    public static final String USER_JWT_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV"
        + "2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjY2QtaW1wb3J"
        + "0QGZha2UuaG1jdHMubmV0IiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkI"
        + "joiZDg3ODI3ODQtMWU0NC00NjkyLTg0NzgtNTI5MzE0NTVhNGI5IiwiaXNzIjoiaHR"
        + "0cDovL2ZyLWFtOjgwODAvb3BlbmFtL29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6I"
        + "mFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI"
        + "6IjNjMWMzNjFkLTRlYzUtNGY0NS1iYzI0LTUxOGMzMDk0MzUxYiIsImF1ZCI6ImNjZ"
        + "F9nYXRld2F5IiwibmJmIjoxNTg0NTI2MzcyLCJncmFudF90eXBlIjoicGFzc3dvcmQ"
        + "iLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiXSwiYXV0aF90aW1lI"
        + "joxNTg0NTI2MzcyLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU4NDU1NTE3MiwiaWF"
        + "0IjoxNTg0NTI2MzcyLCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiNDhjNDMzYTQtZ"
        + "mRiNS00YTIwLWFmNGUtMmYyNjIyYjYzZmU5In0.WP8ATcHMmdtG2W443aqNz3ES6-B"
        + "qng0IKjTQfbndN1HrBLJWJtpC3qfzy2wD_CdiPU4uspdN5S91nhiT8Ub6DjstnDz3V"
        + "PmR3Cbdk5QJBdAsQ0ah9w-duS8SA_dlzDIMt18bSDMUUdck6YxsoNyQFisI6cKNnfg"
        + "B9ZTLhenVENtdmyrKVr96Ezp-jhhzmMVMxb1rW7KghSAH0ZCsWqlhrM--jPGRCweDi"
        + "Fe-ldi4EuhIxGbkPjyWwsdcgmYfIuFrSxqV0vrSI37DNZx_Sh5DVJpUgSrYKRzuMqe"
        + "4rFN6WVyHIY_Qu52ER2yrNYtGbAQ5AyMabPTPj9VVxqpa5nYUAg";
    public static final String USER_ID = "49154ae9-47be-4469-9edd-d43f68d245f0";

    default void addIdamTokenStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/userAuth/o/token"))
                    .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded; charset=UTF-8"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"access_token\": \"" + USER_JWT_TOKEN + "\"}")
                    .build()
            )
        );
    }

    default void addUserInfoStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo("/userAuth/o/userinfo"))
                    .withHeader("Authorization", equalTo("Bearer " + USER_JWT_TOKEN))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"uid\": \"" + USER_ID + "\"}")
                    .build()
            )
        );
    }
}
