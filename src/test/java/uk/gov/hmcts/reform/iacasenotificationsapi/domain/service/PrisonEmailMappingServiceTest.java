package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PrisonEmailMappingServiceTest {

    private ObjectMapper objectMapper;
    private PrisonEmailMappingService prisonEmailMappingService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        prisonEmailMappingService = new PrisonEmailMappingService(objectMapper);
    }

    @Test
    void shouldLoadDevConfigurationByDefault() {
        // Given
        ReflectionTestUtils.setField(prisonEmailMappingService, "environment", "dev");

        // When
        prisonEmailMappingService.loadConfiguration();

        // Then
        Optional<String> email = prisonEmailMappingService.getPrisonEmail("Addiewell");
        assertThat(email).isPresent();
        assertThat(email.get()).isEqualTo("test-det-prison-addiewell@example.com");
    }

    @Test
    void shouldLoadProdConfigurationWhenEnvironmentIsProduction() {
        // Given
        ReflectionTestUtils.setField(prisonEmailMappingService, "environment", "prod");

        // When
        prisonEmailMappingService.loadConfiguration();

        // Then
        Optional<String> email = prisonEmailMappingService.getPrisonEmail("Addiewell");
        assertThat(email).isPresent();
        assertThat(email.get()).isEqualTo("adcourts@sodexogov.co.uk");
    }

    @Test
    void shouldReturnEmptyOptionalWhenPrisonNotFound() {
        // Given
        ReflectionTestUtils.setField(prisonEmailMappingService, "environment", "dev");
        prisonEmailMappingService.loadConfiguration();

        // When
        Optional<String> email = prisonEmailMappingService.getPrisonEmail("NonExistentPrison");

        // Then
        assertThat(email).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptionalWhenMappingsNotLoaded() {
        // Given - don't load configuration

        // When
        Optional<String> email = prisonEmailMappingService.getPrisonEmail("Addiewell");

        // Then
        assertThat(email).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenPrisonIsSupported() {
        // Given
        ReflectionTestUtils.setField(prisonEmailMappingService, "environment", "dev");
        prisonEmailMappingService.loadConfiguration();

        // When
        boolean isSupported = prisonEmailMappingService.isPrisonSupported("Addiewell");

        // Then
        assertThat(isSupported).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPrisonIsNotSupported() {
        // Given
        ReflectionTestUtils.setField(prisonEmailMappingService, "environment", "dev");
        prisonEmailMappingService.loadConfiguration();

        // When
        boolean isSupported = prisonEmailMappingService.isPrisonSupported("NonExistentPrison");

        // Then
        assertThat(isSupported).isFalse();
    }

    @Test
    void shouldReturnFalseWhenMappingsNotLoadedForIsPrisonSupported() {
        // Given - don't load configuration

        // When
        boolean isSupported = prisonEmailMappingService.isPrisonSupported("Addiewell");

        // Then
        assertThat(isSupported).isFalse();
    }

    @Test
    void shouldReturnAllPrisonEmails() {
        // Given
        ReflectionTestUtils.setField(prisonEmailMappingService, "environment", "dev");
        prisonEmailMappingService.loadConfiguration();

        // When
        Map<String, String> allEmails = prisonEmailMappingService.getAllPrisonEmails();

        // Then
        assertThat(allEmails).isNotEmpty();
        assertThat(allEmails).containsKey("Addiewell");
        assertThat(allEmails).containsKey("Birmingham");
        assertThat(allEmails.get("Addiewell")).isEqualTo("test-det-prison-addiewell@example.com");
    }

    @Test
    void shouldReturnEmptyMapWhenMappingsNotLoaded() {
        // Given - don't load configuration

        // When
        Map<String, String> allEmails = prisonEmailMappingService.getAllPrisonEmails();

        // Then
        assertThat(allEmails).isEmpty();
    }

    @Test
    void shouldReturnCurrentEnvironment() {
        // Given
        ReflectionTestUtils.setField(prisonEmailMappingService, "environment", "prod");

        // When
        String environment = prisonEmailMappingService.getEnvironment();

        // Then
        assertThat(environment).isEqualTo("prod");
    }

    @Test
    void shouldFallBackToDevConfigurationWhenConfigFileNotFound() {
        // Given
        ReflectionTestUtils.setField(prisonEmailMappingService, "environment", "nonexistent");

        // When
        prisonEmailMappingService.loadConfiguration();

        // Then
        Optional<String> email = prisonEmailMappingService.getPrisonEmail("Addiewell");
        assertThat(email).isPresent();
        assertThat(email.get()).isEqualTo("test-det-prison-addiewell@example.com"); // Should fall back to dev
    }

    @Test
    void shouldHandleExactPrisonNameMatching() {
        // Given
        ReflectionTestUtils.setField(prisonEmailMappingService, "environment", "dev");
        prisonEmailMappingService.loadConfiguration();

        // When & Then
        assertThat(prisonEmailMappingService.getPrisonEmail("Askham Grange")).isPresent();
        assertThat(prisonEmailMappingService.getPrisonEmail("Hatfield (Main site)")).isPresent();
        assertThat(prisonEmailMappingService.getPrisonEmail("Hatfield (Lakes site)")).isPresent();
    }
} 