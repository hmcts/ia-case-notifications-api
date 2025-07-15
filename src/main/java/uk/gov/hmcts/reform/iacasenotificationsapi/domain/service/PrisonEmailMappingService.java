package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.PrisonEmailMappingConfig;

/**
 * Service to provide prison email address mappings.
 * Loads configuration from JSON files based on environment variable.
 */
@Slf4j
@Service
public class PrisonEmailMappingService {

    private final ObjectMapper objectMapper;
    private Map<String, String> prisonEmailMappings;

    @Value("${prison.email.environment:${PRISON_EMAIL_ENV:dev}}")
    private String environment;

    public PrisonEmailMappingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadConfiguration() {
        try {
            String configFileName = determineConfigFileName();
            log.info("Loading prison email mappings from: {}", configFileName);
            
            ClassPathResource resource = new ClassPathResource(configFileName);
            if (!resource.exists()) {
                log.warn("Prison email configuration file not found: {}, falling back to dev configuration", configFileName);
                resource = new ClassPathResource("prison-emails/prison-emails-dev.json");
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                PrisonEmailMappingConfig config = objectMapper.readValue(inputStream, PrisonEmailMappingConfig.class);
                this.prisonEmailMappings = config.getPrisonEmailMappings();
                log.info("Successfully loaded {} prison email mappings for environment: {}", 
                        prisonEmailMappings.size(), environment);
            }
        } catch (IOException e) {
            log.error("Failed to load prison email mappings for environment: {}", environment, e);
            throw new RuntimeException("Failed to load prison email configuration", e);
        }
    }

    private String determineConfigFileName() {
        // Support multiple environment variable names for flexibility
        String env = Optional.ofNullable(System.getenv("PRISON_EMAIL_ENV"))
                .orElse(Optional.ofNullable(System.getenv("ENVIRONMENT"))
                .orElse(Optional.ofNullable(System.getenv("APP_ENV"))
                .orElse(environment)));
        
        return String.format("prison-emails/prison-emails-%s.json", env);
    }

    /**
     * Get the email address for a given prison name.
     * 
     * @param prisonName the name of the prison
     * @return Optional containing the email address if found
     */
    public Optional<String> getPrisonEmail(String prisonName) {
        if (prisonEmailMappings == null) {
            log.warn("Prison email mappings not loaded, returning empty");
            return Optional.empty();
        }
        
        String email = prisonEmailMappings.get(prisonName);
        if (email == null) {
            log.debug("No email mapping found for prison: {}", prisonName);
            return Optional.empty();
        }
        
        return Optional.of(email);
    }

    /**
     * Get all prison email mappings.
     * 
     * @return Map of prison names to email addresses
     */
    public Map<String, String> getAllPrisonEmails() {
        return prisonEmailMappings != null ? Map.copyOf(prisonEmailMappings) : Map.of();
    }

    /**
     * Check if a prison name exists in the mappings.
     * 
     * @param prisonName the name of the prison
     * @return true if the prison exists in the mappings
     */
    public boolean isPrisonSupported(String prisonName) {
        return prisonEmailMappings != null && prisonEmailMappings.containsKey(prisonName);
    }

    /**
     * Get the current environment configuration.
     * 
     * @return the current environment
     */
    public String getEnvironment() {
        return environment;
    }
} 