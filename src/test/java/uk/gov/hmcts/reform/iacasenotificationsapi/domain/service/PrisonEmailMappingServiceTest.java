package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class PrisonEmailMappingServiceTest {

    private PrisonEmailMappingService prisonEmailMappingService;

    @BeforeEach
    void setUp() {
        prisonEmailMappingService = new PrisonEmailMappingService("dev");
    }

    @Test
    void should_return_email_for_prison_when_environment_variable_exists() {
        try (MockedStatic<System> systemMock = mockStatic(System.class)) {
            systemMock.when(() -> System.getenv("PRISON_DEV_ADDIEWELL_EMAIL"))
                    .thenReturn("test-det-prison-addiewell@example.com");

            prisonEmailMappingService = new PrisonEmailMappingService("dev");
            prisonEmailMappingService.init();

            Optional<String> result = prisonEmailMappingService.getPrisonEmail("Addiewell");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("test-det-prison-addiewell@example.com");
        }
    }

    @Test
    void should_return_empty_when_prison_not_found() {
        prisonEmailMappingService.init();

        Optional<String> result = prisonEmailMappingService.getPrisonEmail("NonexistentPrison");

        assertThat(result).isEmpty();
    }

    @Test
    void should_return_empty_when_prison_name_is_null() {
        prisonEmailMappingService.init();

        Optional<String> result = prisonEmailMappingService.getPrisonEmail(null);

        assertThat(result).isEmpty();
    }

    @Test
    void should_return_empty_when_prison_name_is_empty() {
        prisonEmailMappingService.init();

        Optional<String> result = prisonEmailMappingService.getPrisonEmail("");

        assertThat(result).isEmpty();
    }

    @Test
    void should_return_production_emails_for_prod_environment() {
        try (MockedStatic<System> systemMock = mockStatic(System.class)) {
            systemMock.when(() -> System.getenv("PRISON_PROD_ADDIEWELL_EMAIL"))
                    .thenReturn("adcourts@sodexogov.co.uk");

            PrisonEmailMappingService prodService = new PrisonEmailMappingService("prod");
            prodService.init();

            Optional<String> result = prodService.getPrisonEmail("Addiewell");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("adcourts@sodexogov.co.uk");
        }
    }

    @Test
    void should_check_if_prison_is_supported() {
        try (MockedStatic<System> systemMock = mockStatic(System.class)) {
            systemMock.when(() -> System.getenv("PRISON_DEV_ADDIEWELL_EMAIL"))
                    .thenReturn("test-det-prison-addiewell@example.com");

            prisonEmailMappingService = new PrisonEmailMappingService("dev");
            prisonEmailMappingService.init();

            assertThat(prisonEmailMappingService.isPrisonSupported("Addiewell")).isTrue();
            assertThat(prisonEmailMappingService.isPrisonSupported("NonexistentPrison")).isFalse();
        }
    }

    @Test
    void should_return_all_prison_emails() {
        try (MockedStatic<System> systemMock = mockStatic(System.class)) {
            systemMock.when(() -> System.getenv("PRISON_DEV_ADDIEWELL_EMAIL"))
                    .thenReturn("test-det-prison-addiewell@example.com");
            systemMock.when(() -> System.getenv("PRISON_DEV_ALTCOURSE_EMAIL"))
                    .thenReturn("test-det-prison-altcourse@example.com");

            prisonEmailMappingService = new PrisonEmailMappingService("dev");
            prisonEmailMappingService.init();

            Map<String, String> allEmails = prisonEmailMappingService.getAllPrisonEmails();

            assertThat(allEmails).containsEntry("Addiewell", "test-det-prison-addiewell@example.com");
            assertThat(allEmails).containsEntry("Altcourse", "test-det-prison-altcourse@example.com");
            assertThat(allEmails).hasSize(2);
        }
    }

    @Test
    void should_return_supported_prisons() {
        try (MockedStatic<System> systemMock = mockStatic(System.class)) {
            systemMock.when(() -> System.getenv("PRISON_DEV_ADDIEWELL_EMAIL"))
                    .thenReturn("test-det-prison-addiewell@example.com");

            prisonEmailMappingService = new PrisonEmailMappingService("dev");
            prisonEmailMappingService.init();

            Set<String> supportedPrisons = prisonEmailMappingService.getSupportedPrisons();

            assertThat(supportedPrisons).contains("Addiewell");
            assertThat(supportedPrisons).hasSize(1);
        }
    }

    @Test
    void should_return_correct_environment() {
        assertThat(prisonEmailMappingService.getEnvironment()).isEqualTo("dev");

        PrisonEmailMappingService prodService = new PrisonEmailMappingService("prod");
        assertThat(prodService.getEnvironment()).isEqualTo("prod");
    }

    @Test
    void should_handle_prison_names_with_spaces() {
        try (MockedStatic<System> systemMock = mockStatic(System.class)) {
            systemMock.when(() -> System.getenv("PRISON_DEV_ASKHAM_GRANGE_EMAIL"))
                    .thenReturn("test-det-prison-askham-grange@example.com");

            prisonEmailMappingService = new PrisonEmailMappingService("dev");
            prisonEmailMappingService.init();

            Optional<String> result = prisonEmailMappingService.getPrisonEmail("Askham Grange");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("test-det-prison-askham-grange@example.com");
        }
    }

    @Test
    void should_trim_whitespace_from_prison_name() {
        try (MockedStatic<System> systemMock = mockStatic(System.class)) {
            systemMock.when(() -> System.getenv("PRISON_DEV_ADDIEWELL_EMAIL"))
                    .thenReturn("test-det-prison-addiewell@example.com");

            prisonEmailMappingService = new PrisonEmailMappingService("dev");
            prisonEmailMappingService.init();

            Optional<String> result = prisonEmailMappingService.getPrisonEmail("  Addiewell  ");

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo("test-det-prison-addiewell@example.com");
        }
    }
} 