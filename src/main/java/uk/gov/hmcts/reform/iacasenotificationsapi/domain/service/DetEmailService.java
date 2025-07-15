package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PRISON_NAME;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;

@Service
@Slf4j
public class DetEmailService {

    private final Map<String, String> ircEmailMappings;
    private final PrisonEmailMappingService prisonEmailMappingService;

    @Autowired
    public DetEmailService(Map<String, String> detentionEngagementTeamIrcEmailAddresses,
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

        YesOrNo detentionFacility = asylumCase.read(DETENTION_FACILITY, YesOrNo.class).orElse(YesOrNo.NO);
        
        if (detentionFacility == YesOrNo.YES) {
            return getIrcEmailAddress(asylumCase);
        } else {
            return getPrisonEmailAddress(asylumCase);
        }
    }

    private Optional<String> getIrcEmailAddress(AsylumCase asylumCase) {
        Optional<String> ircName = asylumCase.read(IRC_NAME, String.class);
        
        if (ircName.isEmpty()) {
            log.warn("IRC name is not present in case data for IRC email lookup");
            return Optional.empty();
        }

        String emailAddress = ircEmailMappings.get(ircName.get());
        if (StringUtils.isBlank(emailAddress)) {
            log.warn("No email address found for IRC: {}", ircName.get());
            return Optional.empty();
        }

        log.debug("Found IRC email address for '{}': {}", ircName.get(), emailAddress);
        return Optional.of(emailAddress);
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
