package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;

public interface PersonalisationUtil {

    static String ftpaRespondentDecisionVerbalization(AsylumCase asylumCase) {
        return asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
            .or(() -> asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .map(decision -> decision.equals(FTPA_GRANTED)
                ? "granted"
                : decision.equals(FTPA_PARTIALLY_GRANTED)
                ? "partially granted"
                : "")
            .orElse("");
    }
}
