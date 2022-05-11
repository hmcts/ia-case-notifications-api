package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.DECISION_GRANTED_OR_REFUSED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.SS_CONSENT_DECISION;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

public interface BailEmailNotificationPersonalisation extends BaseNotificationPersonalisation<BailCase> {

    default boolean isBailGranted(BailCase bailCase) {
        return bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class).orElse("").equals("granted")
               || (bailCase.read(RECORD_THE_DECISION_LIST, String.class).orElse("").equals("Minded to grant")
                   && bailCase.read(SS_CONSENT_DECISION, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES);
    }

    default boolean isBailRefused(BailCase bailCase) {
        return bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class).orElse("").equals("refused")
               || bailCase.read(RECORD_THE_DECISION_LIST, String.class).orElse("").equals("Refused")
               || bailCase.read(SS_CONSENT_DECISION, YesOrNo.class).orElse(YesOrNo.YES) == YesOrNo.NO;
    }

    default boolean isLegallyRepresented(BailCase bailCase) {
        return (bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO)) == YesOrNo.YES;
    }
}
