package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isInternalCase;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

/**
 * This bean provides the email address for Detention Engagement Team for unrepresented detained cases.
 */
@Service
public class DetEmailService {

    private final Map<String, String> detentionEngagementTeamIrcEmailAddresses;
    private final EmailAddressFinder emailAddressFinder;

    public DetEmailService(
        Map<String, String> detentionEngagementTeamIrcEmailAddresses, EmailAddressFinder emailAddressFinder
    ) {
        this.detentionEngagementTeamIrcEmailAddresses = detentionEngagementTeamIrcEmailAddresses;
        this.emailAddressFinder = emailAddressFinder;
    }

    public String getDetEmailAddressMapping(Map<String, String> detEmailAddressesMap, String name) {
        String formattedName = name.replaceAll("[^a-zA-Z]","");
        return detEmailAddressesMap.get(formattedName);
    }

    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);

        if (detentionFacility.isEmpty()
            || detentionFacility.get().equals("other")
            || !detentionFacility.get().equals("immigrationRemovalCentre")
            || !isAppellantInDetention(asylumCase)) {
            return Collections.emptySet();
        }

        YesOrNo appellantsRepresentation = asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class).orElse(NO);

        if (isInternalCase(asylumCase) && NO.equals(appellantsRepresentation)) {
            return Collections.singleton(emailAddressFinder.getLegalRepPaperJourneyEmailAddress(asylumCase));
        }

        String retrievedEmailAddress = asylumCase
            .read(IRC_NAME, String.class)
            .map(it -> Optional.ofNullable(getDetEmailAddressMapping(detentionEngagementTeamIrcEmailAddresses, it))
                .orElseThrow(() -> new IllegalStateException("DET email address not found for: " + it)))
            .orElseThrow(() -> new IllegalStateException("IRC name is not present"));

        return Collections.singleton(retrievedEmailAddress);
    }

}
