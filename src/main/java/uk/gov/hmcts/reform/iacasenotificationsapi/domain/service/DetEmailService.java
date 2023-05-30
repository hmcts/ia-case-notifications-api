package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

/**
 * This bean provides the email address for Detention Engagement Team for unrepresented detained cases.
 */
@Service
public class DetEmailService {

    private String adaDetEmailAddress;
    private String detEmailAddressBrookhouse;
    private String detEmailAddressColnbrook;
    private String detEmailAddressDerwentside;
    private String detEmailAddressDungavel;
    private String detEmailAddressHarmondsworth;
    private String detEmailAddressTinsleyHouse;
    private String detEmailAddressYarlswood;

    public DetEmailService(
        @Value("${adaDetUnrepresentedEmailAddress}") String adaDetEmailAddress,
        @Value("${detentionEngagementTeamIrcEmailAddresses.brookhouse}") String detEmailAddressBrookhouse,
        @Value("${detentionEngagementTeamIrcEmailAddresses.colnbrook}") String detEmailAddressColnbrook,
        @Value("${detentionEngagementTeamIrcEmailAddresses.derwentside}") String detEmailAddressDerwentside,
        @Value("${detentionEngagementTeamIrcEmailAddresses.dungavel}") String detEmailAddressDungavel,
        @Value("${detentionEngagementTeamIrcEmailAddresses.harmondsworth}") String detEmailAddressHarmondsworth,
        @Value("${detentionEngagementTeamIrcEmailAddresses.tinsleyhouse}") String detEmailAddressTinsleyHouse,
        @Value("${detentionEngagementTeamIrcEmailAddresses.yarlswood}") String detEmailAddressYarlswood
    ) {
        this.adaDetEmailAddress = adaDetEmailAddress;
        this.detEmailAddressBrookhouse = detEmailAddressBrookhouse;
        this.detEmailAddressColnbrook = detEmailAddressColnbrook;
        this.detEmailAddressDerwentside = detEmailAddressDerwentside;
        this.detEmailAddressDungavel = detEmailAddressDungavel;
        this.detEmailAddressHarmondsworth = detEmailAddressHarmondsworth;
        this.detEmailAddressTinsleyHouse = detEmailAddressTinsleyHouse;
        this.detEmailAddressYarlswood = detEmailAddressYarlswood;
    }

    public String getDetEmailAddress(AsylumCase asylumCase) {

        String ircName = asylumCase.read(IRC_NAME, String.class).orElse("");

        switch (ircName) {
            case "Brookhouse":
                return detEmailAddressBrookhouse;
            case "Colnbrook":
                return detEmailAddressColnbrook;
            case "Derwentside":
                return detEmailAddressDerwentside;
            case "Dungavel":
                return detEmailAddressDungavel;
            case "Harmondsworth":
                return detEmailAddressHarmondsworth;
            case "Tinsley House":
                return detEmailAddressTinsleyHouse;
            case "Yarlswood":
                return detEmailAddressYarlswood;
            // Default case captures cases in 'Prison' or 'Other' detentions facilities
            default:
                return adaDetEmailAddress;
        }
    }
}
