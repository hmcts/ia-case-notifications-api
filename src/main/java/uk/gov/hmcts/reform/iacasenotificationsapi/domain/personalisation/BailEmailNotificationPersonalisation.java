package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

public interface BailEmailNotificationPersonalisation extends BaseNotificationPersonalisation<BailCase> {

    default boolean isBailGranted(BailCase bailCase) {
        return bailCase.read(RECORD_DECISION_TYPE, String.class).orElse("").equals("granted")
               || bailCase.read(RECORD_DECISION_TYPE, String.class).orElse("").equals("conditionalGrant");
    }

    default boolean isBailRefused(BailCase bailCase) {
        return bailCase.read(RECORD_DECISION_TYPE, String.class).orElse("").equals("refused");
    }

    default boolean isLegallyRepresented(BailCase bailCase) {
        return (bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO)) == YesOrNo.YES;
    }
}
