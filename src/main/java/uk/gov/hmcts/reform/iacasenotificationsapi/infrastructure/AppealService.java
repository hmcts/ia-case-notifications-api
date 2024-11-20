package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_DECISION_WITHOUT_HEARING;

@Service
public class AppealService {

    public boolean isAppealListed(AsylumCase asylumCase) {

        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);
        final boolean decisionWithoutHearing = asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class)
                .map(yesOrNo -> YesOrNo.YES == yesOrNo).orElse(false);

        return appealListed.isPresent() || decisionWithoutHearing;
    }

    public boolean isAppellantInPersonJourney(AsylumCase asylumCase) {
        return  asylumCase
            .read(AsylumCaseDefinition.JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == JourneyType.AIP).orElse(false);
    }
}
