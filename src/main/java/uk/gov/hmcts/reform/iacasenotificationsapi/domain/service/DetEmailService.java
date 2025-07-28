package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PRISON_NAME;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;

@Service
@Slf4j
public class DetEmailService {

    private final Map<String, String> ircEmailMappings;
    private final PrisonEmailMappingService prisonEmailMappingService;

    @Autowired
    public DetEmailService(@Qualifier("detentionEngagementTeamIrcEmailAddresses") Map<String, String> detentionEngagementTeamIrcEmailAddresses,
                           PrisonEmailMappingService prisonEmailMappingService) {
        this.ircEmailMappings = detentionEngagementTeamIrcEmailAddresses;
        this.prisonEmailMappingService = prisonEmailMappingService;
    }

    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return getDetEmailAddress(asylumCase)
            .map(Set::of)
            .orElse(Collections.emptySet());
    }

    public Optional<String> getDetEmailAddress(AsylumCase asylumCase) {
        
        if (!AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase)) {
            log.debug("Case is not an accelerated detained appeal, no DET email required");
            return Optional.empty();
        }

        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);

        if (detentionFacility.isPresent()) {
            String facility = detentionFacility.get();
            switch (facility) {
                case "immigrationRemovalCentre":
                    return getIrcEmailAddress(asylumCase);
                case "prison":
                    return getPrisonEmailAddress(asylumCase);
                case "other":
                    log.debug("Detention facility is 'other', no email address available");
                    return Optional.empty();
                default:
                    log.warn("Unknown detention facility type: '{}', no email address available", facility);
                    return Optional.empty();
            }
        } else {
            log.warn("DETENTION_FACILITY field is not present in case data");
            return Optional.empty();
        }
    }

    private Optional<String> getIrcEmailAddress(AsylumCase asylumCase) {
        String emailAddress = asylumCase
            .read(IRC_NAME, String.class)
            .map(ircName -> {
                String email = getDetEmailAddressMapping(ircEmailMappings, ircName);
                if (email == null) {
                    throw new IllegalStateException("DET email address not found for: " + ircName);
                }
                return email;
            })
            .orElseThrow(() -> new IllegalStateException("IRC name is not present"));
        
        return Optional.of(emailAddress);
    }

    /**
     * Gets the email address mapping for a given key from the provided mappings.
     * 
     * @param mappings the email mappings
     * @param key the key to look up
     * @return the email address, or null if not found
     */
    private String getDetEmailAddressMapping(Map<String, String> mappings, String key) {
        return mappings.get(key);
    }

    private Optional<String> getPrisonEmailAddress(AsylumCase asylumCase) {
        Optional<String> prisonName = asylumCase.read(PRISON_NAME, String.class);
        
        if (prisonName.isEmpty()) {
            log.warn("Prison name is not present in case data for prison email lookup");
            return Optional.empty();
        }

        Optional<String> emailAddress = prisonEmailMappingService.getPrisonEmail(prisonName.get());
        if (emailAddress.isEmpty()) {
            log.warn("No email address found for prison: {}", prisonName.get());
            return Optional.empty();
        }

        log.debug("Found prison email address for '{}': {}", prisonName.get(), emailAddress.get());
        return emailAddress;
    }
}
