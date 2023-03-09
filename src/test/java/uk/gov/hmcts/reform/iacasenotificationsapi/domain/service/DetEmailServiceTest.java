package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DetEmailServiceTest {
    private final String dummyAdaDetEmailAddress = "example@email.com";
    private DetEmailService detEmailService;

    @BeforeEach
    void setUp() {
        detEmailService = new DetEmailService(dummyAdaDetEmailAddress);
    }

    @Test
    void should_return_det_email_address_for_unrepresented_cases() {
        final String adaDetUnrepresentedEmailAddress = detEmailService.getAdaDetEmailAddress();
        assertEquals(dummyAdaDetEmailAddress, adaDetUnrepresentedEmailAddress);
    }

}