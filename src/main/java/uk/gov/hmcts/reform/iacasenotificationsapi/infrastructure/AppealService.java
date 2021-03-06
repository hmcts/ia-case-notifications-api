package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;


@Service
public class AppealService {

    public boolean isAppealListed(AsylumCase asylumCase) {

        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }

    public boolean isAppellantInPersonJourney(AsylumCase asylumCase) {
        return  asylumCase
            .read(AsylumCaseDefinition.JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == JourneyType.AIP).orElse(false);
    }
}
