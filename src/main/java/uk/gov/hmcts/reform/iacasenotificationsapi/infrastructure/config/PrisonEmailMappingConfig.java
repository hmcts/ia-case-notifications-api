package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Configuration class for prison email mappings loaded from JSON files.
 */
public class PrisonEmailMappingConfig {
    
    @JsonProperty("prisonEmailMappings")
    private Map<String, String> prisonEmailMappings;
    
    public PrisonEmailMappingConfig() {
        // Default constructor for Jackson
    }
    
    public Map<String, String> getPrisonEmailMappings() {
        return prisonEmailMappings;
    }
    
    public void setPrisonEmailMappings(Map<String, String> prisonEmailMappings) {
        this.prisonEmailMappings = prisonEmailMappings;
    }
} 