package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers;

import static org.junit.Assert.*;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class WelcomeControllerTest {

    private final WelcomeController welcomeController = new WelcomeController();

    @Test
    public void should_return_welcome_response() {

        ResponseEntity<String> responseEntity = welcomeController.welcome();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertThat(responseEntity.getBody()).contains("Welcome to Immigration & Asylum case notifications API");
    }
}
