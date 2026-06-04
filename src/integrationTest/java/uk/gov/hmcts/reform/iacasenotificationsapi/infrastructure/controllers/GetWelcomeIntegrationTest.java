package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.SpringBootIntegrationTest;

class GetWelcomeIntegrationTest extends SpringBootIntegrationTest {

    @Test
    void welcomeRootEndpoint() throws Exception {
        MvcResult response = mockMvc
            .perform(get("/"))
            .andExpect(status().isOk())
            .andReturn();

        assertTrue(response.getResponse().getContentAsString()
            .contains("Welcome to Immigration & Asylum case notifications API"));
    }
}
