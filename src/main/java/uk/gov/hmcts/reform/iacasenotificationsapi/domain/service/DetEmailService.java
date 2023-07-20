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
    private final Map<String, String> detentionEngagementTeamPrisonEmailAddresses;


    public DetEmailService(
        Map<String, String> detentionEngagementTeamIrcEmailAddresses,
        Map<String, String> detentionEngagementTeamPrisonEmailAddresses
    ) {
        this.detentionEngagementTeamIrcEmailAddresses = detentionEngagementTeamIrcEmailAddresses;
        this.detentionEngagementTeamPrisonEmailAddresses = detentionEngagementTeamPrisonEmailAddresses;
    }

    public String getDetEmailAddressMapping(Map<String, String> detEmailAddressesMap, String name) {
        String formattedName = name.replaceAll("\\s|[^a-zA-Z]","");
        return detEmailAddressesMap.get(formattedName);
    }

    public String getDetEmailAddress(AsylumCase asylumCase) {
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);

        return detentionFacility.get().equals("immigrationRemovalCentre")
            ?
            asylumCase
                .read(IRC_NAME, String.class)
                .map(it -> Optional.ofNullable(getDetEmailAddressMapping(detentionEngagementTeamIrcEmailAddresses, it))
                    .orElseThrow(() -> new IllegalStateException("DET email address not found for: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException("IRC name is not present"))
            :
            asylumCase
                .read(PRISON_NAME, String.class)
                .map(it -> Optional.ofNullable(getDetEmailAddressMapping(detentionEngagementTeamPrisonEmailAddresses, it))
                    .orElseThrow(() -> new IllegalStateException("DET email address not found for: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException("Prison name is not present"));
    }

}
