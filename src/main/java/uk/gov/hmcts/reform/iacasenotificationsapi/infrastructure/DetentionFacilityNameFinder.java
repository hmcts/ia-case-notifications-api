package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@Service
public class DetentionFacilityNameFinder {
    private static final String PRISON_NAME = "prisonName";
    private static final String IRC_NAME = "ircName";

    private final StringProvider stringProvider;

    public DetentionFacilityNameFinder(StringProvider stringProvider) {
        this.stringProvider = stringProvider;
    }

    public String getDetentionFacility(String detentionFacilityName) {
        return stringProvider.get(PRISON_NAME, detentionFacilityName)
            .orElse(stringProvider.get(IRC_NAME, detentionFacilityName)
                .orElse(detentionFacilityName));
    }
}
