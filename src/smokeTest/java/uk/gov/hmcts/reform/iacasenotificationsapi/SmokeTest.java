package uk.gov.hmcts.reform.iacasenotificationsapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.http.HttpStatus;

@Slf4j
public class SmokeTest {

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8093"
        );

    @Test
    public void should_prove_app_is_running_and_healthy() {

        log.info("\n\n Smoke test request to url: {} ", targetInstance);

        String s2s = StringUtils.defaultIfBlank(
            System.getenv("S2S_URL"),
            "http://localhost:8093"
        );

        log.info("\n\n S2S url: {} ", s2s);

        Properties properties = System.getProperties();
        properties.forEach((o, o2) -> log.info("name: {}, value: {}", o, o2));

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        String response = SerenityRest
            .when()
            .get("/health")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();

        assertThat(response)
            .contains("UP");
    }
}
