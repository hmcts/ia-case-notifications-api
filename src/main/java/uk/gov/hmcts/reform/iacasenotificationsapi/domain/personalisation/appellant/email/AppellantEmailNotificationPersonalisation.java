package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;


import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;

public interface AppellantEmailNotificationPersonalisation extends EmailNotificationPersonalisation {

    @Override
    default Set<String> getRecipientsList(AsylumCase asylumCase) {

        if (asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> Objects.equals(type.getValue(), JourneyType.AIP.getValue()))
            .orElse(false)) {

            return Collections.singleton(asylumCase
                .read(APPELLANT_EMAIL_ADDRESS, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantEmailAddress is not present")));


        } else {

            return Collections.emptySet();


        }

    }
}
