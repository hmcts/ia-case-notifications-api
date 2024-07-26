package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.*;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

public class AsylumCaseUtils {

    private AsylumCaseUtils() {
        // private constructor to prevent sonar warning
    }

    public static boolean isIntegrated(AsylumCase asylumCase) {
        return asylumCase.read(IS_INTEGRATED, YesOrNo.class)
            .map(isIntegrated -> isIntegrated.equals(YES))
            .orElse(false);
    }
}
