package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;

/**
 * Service for mapping prison names to email addresses.
 * Reads prison email addresses from environment variables stored in Azure Key Vault.
 * 
 * Environment Variables Pattern:
 * - Development: PRISON_DEV_{PRISON_NAME}_EMAIL
 * - Production: PRISON_PROD_{PRISON_NAME}_EMAIL
 * 
 * Example:
 * - PRISON_DEV_ADDIEWELL_EMAIL=test-det-prison-addiewell@example.com
 * - PRISON_PROD_ADDIEWELL_EMAIL=adcourts@sodexogov.co.uk
 */
@Service
public class PrisonEmailMappingService {

    private final String environment;
    private final Map<String, String> prisonEmailCache = new HashMap<>();

    // All supported prison names (normalized to uppercase with underscores)
    private static final String[] SUPPORTED_PRISONS = {
        "ADDIEWELL", "ALTCOURSE", "ASKHAM_GRANGE", "AYLESBURY", "BEDFORD", "BELMARSH",
        "BERWYN", "BIRMINGHAM", "BLANTYRE_HOUSE", "BRIXTON", "BROCKHILL", "BRONZEFIELD",
        "BUCKLEY_HALL", "BULLINGDON", "BURE", "CARDIFF", "CHANNINGS_WOOD", "CHELMSFORD",
        "COLDINGLEY", "COOKHAM_WOOD", "DARTMOOR", "DEERBOLT", "DOWNVIEW", "DRAKE_HALL",
        "DURHAM", "EAST_SUTTON_PARK", "EASTWOOD_PARK", "ERLESTOKE", "EVERTHORPE",
        "EXETER", "FEATHERSTONE", "FELTHAM", "FORD", "FOREST_BANK", "FOSTON_HALL",
        "FRANKLAND", "FULL_SUTTON", "GARTH", "GARTREE", "GLEN_PARVA", "GLOUCESTER",
        "GRENDON", "GUYS_MARSH", "HATFIELD", "HAVERIGG", "HEWELL", "HIGH_DOWN",
        "HINDLEY", "HOLLESLEY_BAY", "HOLME_HOUSE", "HULL", "HUNTERCOMBE", "HUMBER",
        "ISIS", "ISLE_OF_WIGHT", "KENNET", "KIRKHAM", "KIRKLEVINGTON_GRANGE", "LANCASTER_FARMS",
        "LEEDS", "LEICESTER", "LEWES", "LEYHILL", "LINCOLN", "LINDHOLME", "LITTLEHEY",
        "LIVERPOOL", "LONG_LARTIN", "LOWDHAM_GRANGE", "MAIDSTONE", "MANCHESTER",
        "MOORLAND", "MORTON_HALL", "MOUNT", "NEW_HALL", "NORTH_SEA_CAMP", "NORTHUMBERLAND",
        "NORWICH", "NOTTINGHAM", "OAKWOOD", "ONLEY", "PARC", "PARKHURST", "PENTONVILLE",
        "PETERBOROUGH", "PORTLAND", "PRESTON", "RANBY", "READING", "RISLEY", "ROCHESTER",
        "RYE_HILL", "SEND", "SHEPTON_MALLET", "SPRING_HILL", "STAFFORD", "STANDFORD_HILL",
        "STOCKEN", "STOKE_HEATH", "STYAL", "SUDBURY", "SWALESIDE", "SWANSEA", "SWINFEN_HALL",
        "THAMESIDE", "THE_MOUNT", "THE_VERNE", "THORN_CROSS", "THURROCK", "USK", "WAKEFIELD",
        "WANDSWORTH", "WARREN_HILL", "WAYLAND", "WEALSTUN", "WELLINGBOROUGH", "WERRINGTON",
        "WETHERBY", "WHATTON", "WHITEMOOR", "WINCHESTER", "WOODHILL", "WORMWOOD_SCRUBS",
        "WYMOTT"
    };

    public PrisonEmailMappingService(
        @Value("${prison.email.environment:${PRISON_EMAIL_ENV:${ENVIRONMENT:${APP_ENV:dev}}}}") String environment
    ) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        loadPrisonEmails();
    }

    /**
     * Loads prison email addresses from environment variables into cache.
     * Environment variable pattern: PRISON_{ENV}_{PRISON_NAME}_EMAIL
     */
    private void loadPrisonEmails() {
        String envPrefix = "dev".equals(environment) ? "DEV" : "PROD";
        
        for (String prisonName : SUPPORTED_PRISONS) {
            String envVarName = String.format("PRISON_%s_%s_EMAIL", envPrefix, prisonName);
            String emailAddress = System.getenv(envVarName);
            
            if (emailAddress != null && !emailAddress.trim().isEmpty()) {
                // Convert back to readable format for the key (e.g., "ADDIEWELL" -> "Addiewell")
                String readablePrisonName = convertToReadableFormat(prisonName);
                prisonEmailCache.put(readablePrisonName, emailAddress.trim());
            }
        }
    }

    /**
     * Converts environment variable format to readable prison name.
     * Example: "ADDIEWELL" -> "Addiewell", "ASKHAM_GRANGE" -> "Askham Grange"
     */
    private String convertToReadableFormat(String envFormat) {
        String[] words = envFormat.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            String word = words[i];
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            }
        }
        
        return result.toString();
    }

    /**
     * Gets the email address for a specific prison.
     * 
     * @param prisonName The name of the prison (e.g., "Addiewell", "Askham Grange")
     * @return Optional containing the email address if found, empty otherwise
     */
    public Optional<String> getPrisonEmail(String prisonName) {
        if (prisonName == null || prisonName.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(prisonEmailCache.get(prisonName.trim()));
    }

    /**
     * Checks if a prison is supported (has an email mapping).
     * 
     * @param prisonName The name of the prison
     * @return true if the prison is supported, false otherwise
     */
    public boolean isPrisonSupported(String prisonName) {
        return getPrisonEmail(prisonName).isPresent();
    }

    /**
     * Gets all prison email mappings.
     * 
     * @return Map of prison names to email addresses
     */
    public Map<String, String> getAllPrisonEmails() {
        return new HashMap<>(prisonEmailCache);
    }

    /**
     * Gets all supported prison names.
     * 
     * @return Set of prison names that have email mappings
     */
    public Set<String> getSupportedPrisons() {
        return prisonEmailCache.keySet();
    }

    /**
     * Gets the current environment configuration.
     * 
     * @return The environment (dev/prod) being used for email mapping
     */
    public String getEnvironment() {
        return environment;
    }
} 