package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J;

public class Stf24WeeksUtil {
    public static final int DAYS_14 = 14;
    public static final int DAYS_42 = 42;
    public static final int DAYS_56 = 56;
    public static final String DAYS_14_FROM_DATE_OF_DIRECTION_KEY = "14DaysFromDateOfDirection";
    public static final String DAYS_56_FROM_DATE_OF_DIRECTION = "56DaysFromDateOfDirection";
    public static final String DAYS_42_FROM_DATE_OF_DIRECTION_KEY = "42DaysFromDateOfDirection";
    public static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL = "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL";
    public static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER = "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER";
    public static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS = "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_SMS";
    public static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL = "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL";
    public static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL = "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL";

    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL";

    public static final String WEEKS_DEADLINE = "24WeeksDeadline";
    public static final String DECISION_SENT_DATE = "decisionSentDate";
    public static final String PRACTICE_DIRECTION = "practiceDirection";
    public static final String APPEAL_RECEIVED_DATE = "appealReceivedDate";
    public static final String APPELLANT_FULL_NAME = "appellantFullName";
    public static final String EMPTY_STRING = "";
    public static final String HOME_OFFICE_REFERENCE_NUMBER_KEY = "homeOfficeReferenceNumber";
    public static final String LEGAL_REP_REFERENCE_NUMBER_KEY = "legalRepReferenceNumber";

    private Stf24WeeksUtil() {
    }

    public static String getAppellantGivenName(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING);
    }

    public static String getAppellantFamilyName(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING);
    }

    public static String getLegalRepReferenceNo(AsylumCase asylumCase) {
        String legalRepReference = asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING);
        if (legalRepReference.equals(EMPTY_STRING)) {
            legalRepReference = asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class).orElse(EMPTY_STRING);
        }
        return legalRepReference;
    }

    public static boolean noLegalRepresentation(AsylumCase asylumCase) {
        String legalRepRefNo = Stf24WeeksUtil.getLegalRepReferenceNo(asylumCase);
        return legalRepRefNo.isEmpty();
    }
}
