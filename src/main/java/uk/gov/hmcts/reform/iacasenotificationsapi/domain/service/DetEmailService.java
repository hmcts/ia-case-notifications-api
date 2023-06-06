package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

/**
 * This bean provides the email address for Detention Engagement Team for unrepresented detained cases.
 */
@Service
public class DetEmailService {

    private final Map<String, String> detentionEngagementTeamIrcEmailAddresses;

    public DetEmailService(
        Map<String, String> detentionEngagementTeamIrcEmailAddresses
    ) {
        this.detentionEngagementTeamIrcEmailAddresses = detentionEngagementTeamIrcEmailAddresses;
    }

    public String getDetEmailAddressMapping(Map<String, String> detEmailAddressesMap, String ircName) {
        String formattedIrcName = ircName.replaceAll(" ", "");
        return detEmailAddressesMap.get(formattedIrcName);
    }

    public String getDetEmailAddress(AsylumCase asylumCase) {
        return asylumCase
            .read(IRC_NAME, String.class)
            .map(it -> Optional.ofNullable(getDetEmailAddressMapping(detentionEngagementTeamIrcEmailAddresses, it))
                .orElseThrow(() -> new IllegalStateException("DET email address not found for: " + it.toString()))
            )
            .orElseThrow(() -> new IllegalStateException("IRC name is not present"));
    }
}
