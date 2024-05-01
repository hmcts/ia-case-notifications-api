package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

public class AsylumCaseUtils {

    private AsylumCaseUtils() {
    }

    public static boolean isAppellantInDetention(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO).equals(YES);
    }

    public static boolean isInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_ADMIN, YesOrNo.class).map(isAdmin -> YES == isAdmin).orElse(false);
    }

    public static boolean isSubmissionOutOfTime(AsylumCase asylumCase) {
        return asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class).orElse(NO).equals(YES);
    }
}
