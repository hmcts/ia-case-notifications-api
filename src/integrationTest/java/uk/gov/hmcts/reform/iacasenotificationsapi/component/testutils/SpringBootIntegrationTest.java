package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.iacasenotificationsapi.Application;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(classes = {
    TestConfiguration.class,
    Application.class
})
@TestPropertySource(properties = {
    "CCD_URL=http://127.0.0.1:8990/ccd",
    "IDAM_URL=http://127.0.0.1:8990/userAuth",
    "OPEN_ID_IDAM_URL=http://127.0.0.1:8990/userAuth",
    "DM_URL=http://127.0.0.1:8990",
    "S2S_URL=http://127.0.0.1:8990/serviceAuth",
    "IA_CASE_DOCUMENTS_API_URL=http://localhost:8990/ia-case-documents-api"
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SpringBootIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext wac;

    protected static WireMockServer server;

    @BeforeAll
    public void spinUp() {
        server = new WireMockServer(WireMockConfiguration.options()
            .notifier(new Slf4jNotifier(true))
            .port(8990));
        server.start();
    }

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(wac).build();
    }

    @AfterEach
    public void reset() {
        server.resetAll();
    }

    @AfterAll
    public void shutDown() {
        server.stop();
    }

}

