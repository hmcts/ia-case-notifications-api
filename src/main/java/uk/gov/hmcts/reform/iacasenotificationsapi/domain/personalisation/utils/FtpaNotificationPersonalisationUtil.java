package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;

import java.util.Optional;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;

public interface FtpaNotificationPersonalisationUtil {

    default Optional<FtpaDecisionOutcomeType> ftpaRespondentLjRjDecision(AsylumCase asylumCase) {
        return asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
            .or(() -> asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class));
    }

    default String camelCaseToSentenceCase(FtpaDecisionOutcomeType decisionType) {
        switch (decisionType) {
            case FTPA_GRANTED:
                return "granted";
            case FTPA_PARTIALLY_GRANTED:
                return "partially granted";
            case FTPA_REFUSED:
                return "refused";
            case FTPA_NOT_ADMITTED:
                return "not admitted";
            default:
                return "";
        }
    }

    default String ftpaDecisionVerbalization(AsylumCase asylumCase) {
        return ftpaRespondentLjRjDecision(asylumCase)
            .map(this::camelCaseToSentenceCase)
            .orElse("");
    }
}
