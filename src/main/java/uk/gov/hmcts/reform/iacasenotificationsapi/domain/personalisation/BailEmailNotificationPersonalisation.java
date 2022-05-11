package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Optional;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

public interface BailEmailNotificationPersonalisation extends BaseNotificationPersonalisation<BailCase> {

    default String decision(BailCase bailCase) {
        String decision = bailCase.read(BailCaseFieldDefinition.RECORD_DECISION_TYPE, String.class).orElse("");

        return decision.equals("granted") || decision.equals("conditionalGrant")
            ? "Granted"
            : decision.equals("refused")
            ? "Refused"
            : "";
    }

    default boolean isBailRefused(BailCase bailCase) {
        return bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class).orElse("").equals("refused") ||
               bailCase.read(RECORD_THE_DECISION_LIST, String.class).orElse("").equals("Refused") ||
               bailCase.read(SS_CONSENT_DECISION, YesOrNo.class).orElse(YesOrNo.YES) == YesOrNo.NO;
    }

    default boolean isLegallyRepresented(BailCase bailCase) {
        return (bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO)) == YesOrNo.YES;
    }
}
