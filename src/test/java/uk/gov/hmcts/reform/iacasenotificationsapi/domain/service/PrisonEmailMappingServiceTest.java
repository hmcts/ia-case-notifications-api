package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;


import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class PrisonEmailMappingServiceTest {

    private PrisonEmailMappingService prisonEmailMappingService;

    @BeforeEach
    void setUp() {
        String validJsonData = """
            {
              "prisonEmailMappings": {
                "Addiewell": "addiewell@example.com",
                "Belmarsh": "belmarsh@example.com",
                "Askham Grange": "askham-grange@example.com"
              }
            }
            """;
        prisonEmailMappingService = new PrisonEmailMappingService(validJsonData);
        prisonEmailMappingService.init();
    }

    @Test
    void should_return_email_for_prison_when_mapping_exists() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail("Addiewell");

        assertTrue(result.isPresent());
        assertEquals("addiewell@example.com", result.get());
    }

    @Test
    void should_return_empty_when_prison_not_found() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail("NonexistentPrison");

        assertTrue(result.isEmpty());
    }

    @Test
    void should_return_empty_when_prison_name_is_null() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void should_return_empty_when_prison_name_is_empty() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail("");

        assertTrue(result.isEmpty());
    }

    @Test
    void should_handle_empty_configuration() {
        String emptyJsonData = """
            {
              "prisonEmailMappings": {}
            }
            """;
        PrisonEmailMappingService emptyService = new PrisonEmailMappingService(emptyJsonData);
        emptyService.init();

        Optional<String> result = emptyService.getPrisonEmail("Addiewell");

        assertTrue(result.isEmpty());
    }

    @Test
    void should_handle_no_configuration() {
        PrisonEmailMappingService noConfigService = new PrisonEmailMappingService("");
        noConfigService.init();

        Optional<String> result = noConfigService.getPrisonEmail("Addiewell");

        assertTrue(result.isEmpty());
    }

    @Test
    void should_handle_invalid_json() {
        PrisonEmailMappingService invalidJsonService = new PrisonEmailMappingService("invalid json");
        invalidJsonService.init();

        Optional<String> result = invalidJsonService.getPrisonEmail("Addiewell");

        assertTrue(result.isEmpty());
    }

    @Test
    void should_check_if_prison_is_supported() {
        assertTrue(prisonEmailMappingService.isPrisonSupported("Addiewell"));
        assertFalse(prisonEmailMappingService.isPrisonSupported("NonexistentPrison"));
    }

    @Test
    void should_return_all_prison_emails() {
        Map<String, String> allEmails = prisonEmailMappingService.getAllPrisonEmails();

        assertEquals("addiewell@example.com", allEmails.get("Addiewell"));
        assertEquals("belmarsh@example.com", allEmails.get("Belmarsh"));
        assertEquals("askham-grange@example.com", allEmails.get("Askham Grange"));
        assertEquals(3, allEmails.size());
    }

    @Test
    void should_return_supported_prisons() {
        Set<String> supportedPrisons = prisonEmailMappingService.getSupportedPrisons();

        assertTrue(supportedPrisons.containsAll(List.of("Addiewell", "Belmarsh", "Askham Grange")));
        assertEquals(3, supportedPrisons.size());
    }

    @Test
    void should_handle_prison_names_with_spaces() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail("Askham Grange");

        assertTrue(result.isPresent());
        assertEquals("askham-grange@example.com", result.get());
    }

    @Test
    void should_trim_whitespace_from_prison_name() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail("  Addiewell  ");

        assertTrue(result.isPresent());
        assertEquals("addiewell@example.com", result.get());
    }

    @Test
    void should_handle_malformed_json_structure() {
        String malformedJson = """
            {
              "wrongKey": {
                "Addiewell": "addiewell@example.com"
              }
            }
            """;

        PrisonEmailMappingService malformedService = new PrisonEmailMappingService(malformedJson);
        malformedService.init();

        Optional<String> result = malformedService.getPrisonEmail("Addiewell");

        assertTrue(result.isEmpty());
    }
}