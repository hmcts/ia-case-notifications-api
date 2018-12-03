package uk.gov.hmcts.reform.iacasenotificationsapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security.IdamAuthorizor;

@Service
public class AuthorizationHeadersProvider {

    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Autowired
    private IdamAuthorizor idamAuthorizor;

    public Headers getCaseOfficerAuthorization() {

        String serviceToken = serviceAuthTokenGenerator.generate();
        String accessToken = idamAuthorizor.exchangeForAccessToken(
            System.getenv("TEST_CASEOFFICER_USERNAME"),
            System.getenv("TEST_CASEOFFICER_PASSWORD")
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }
}
