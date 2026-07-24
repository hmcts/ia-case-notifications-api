package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.D_MMM_YYYY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.buildAddressForAppellantIccLetter;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.buildAddressForLegalRepIccLetter;

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
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_LETTER = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_LETTER";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_EMAIL = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LEGAL_REP_COPY_EMAIL";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL = "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL";

    public static final String WEEKS_DEADLINE = "24WeeksDeadline";
    public static final String DECISION_SENT_DATE = "decisionSentDate";
    public static final String PRACTICE_DIRECTION = "practiceDirection";
    public static final String APPEAL_RECEIVED_DATE = "appealReceivedDate";
    public static final String APPELLANT_FULL_NAME = "appellantFullName";
    public static final String EMPTY_STRING = "";
    public static final String HOME_OFFICE_REFERENCE_NUMBER_KEY = "homeOfficeReferenceNumber";
    public static final String LEGAL_REP_REFERENCE_NUMBER_KEY = "legalRepReferenceNumber";

    private static final String SUBJECT_PREFIX_KEY = "subjectPrefix";
    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";

    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    public static final String FYI_HEADING = "fyiHeading";
    public static final String FYI_TEXT = "fyiText";
    public static final String FYI_LINE_SEPARATOR = "fyiLineSeparator";

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

    public static ImmutableMap<String, String> buildParams(AsylumCase asylumCase, CustomerServicesProvider customerServicesProvider, String nonAdaPrefix, boolean isAppellant) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        requireNonNull(asylumCase, "asylumCase must not be null");
        String givenNames = asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING);
        String familyName = asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING);
        builder.put(SUBJECT_PREFIX_KEY, nonAdaPrefix)
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
                .put(HOME_OFFICE_REFERENCE_NUMBER_KEY, asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put(APPEAL_REFERENCE_NUMBER_KEY, asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING))
                .put(LEGAL_REP_REFERENCE_NUMBER_KEY, getLegalRepReferenceNo(asylumCase))
                .put(APPELLANT_GIVEN_NAMES_KEY, givenNames)
                .put(APPELLANT_FAMILY_NAME_KEY, familyName)
                .put(APPEAL_RECEIVED_DATE, AsylumCaseUtils.getAppealReceivedDate(asylumCase))
                .put(DECISION_SENT_DATE, AsylumCaseUtils.getHomeOfficeDecisionDate(asylumCase))
                .put(WEEKS_DEADLINE, AsylumCaseUtils.populateStatutoryTimeFrame24wDate(asylumCase));
        populate24WeeksDates(builder);
        if (isAppellant) {
            buildFyiAppellantContent(builder, givenNames, familyName);
        } else {
            buildLrFyiContent(builder);
        }
        return builder.build();
    }


    public static ImmutableMap<String, String> buildLetterParams(AsylumCase asylumCase, CustomerServicesProvider customerServicesProvider, boolean isAppellant) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        String givenNames = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING);
        String familyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING);
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
                .put(APPEAL_REFERENCE_NUMBER_KEY, asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING))
                .put(LEGAL_REP_REFERENCE_NUMBER_KEY, asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)
                        .filter(ref -> !ref.isEmpty())
                        .orElseGet(() -> asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class).orElse(EMPTY_STRING)))
                .put(APPELLANT_GIVEN_NAMES_KEY, givenNames)
                .put(APPELLANT_FAMILY_NAME_KEY, familyName)
                .put(HOME_OFFICE_REFERENCE_NUMBER_KEY, asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(EMPTY_STRING))
                .put(APPEAL_RECEIVED_DATE, AsylumCaseUtils.getAppealReceivedDate(asylumCase))
                .put(DECISION_SENT_DATE, AsylumCaseUtils.getHomeOfficeDecisionDate(asylumCase))
                .put(WEEKS_DEADLINE, AsylumCaseUtils.populateStatutoryTimeFrame24wDate(asylumCase));
        populate24WeeksDates(builder);
        if (isAppellant) {
            buildAddressForAppellantIccLetter(asylumCase, builder);
            buildFyiAppellantContent(builder, givenNames, familyName);
        } else {
            buildAddressForLegalRepIccLetter(asylumCase, builder);
            buildLrFyiContent(builder);
        }
        return builder.build();
    }

    private static void buildLrFyiContent(ImmutableMap.Builder<String, String> builder) {
        builder.put(FYI_HEADING, EMPTY_STRING);
        builder.put(FYI_TEXT, EMPTY_STRING);
        builder.put(FYI_LINE_SEPARATOR, EMPTY_STRING);
    }

    private static void buildFyiAppellantContent(ImmutableMap.Builder<String, String> builder, String givenNames, String familyName) {
        builder.put(FYI_HEADING, "#For your information");
        builder.put(FYI_TEXT, String.format("Dear %s %s, \n", givenNames, familyName) +
                "The Tribunal has issued directions in your appeal that have been sent to your legal representative. We are also sending you a copy to ensure that you are aware of what will happen in your appeal.\n\n" +
                "Your legal representative is responsible for progressing your case and responding to these directions on your behalf. You should contact them if you have any questions.\n\n" +
                "If you cease to be represented, or if your contact details change, you must inform the Tribunal as soon as possible.\n");
        builder.put(FYI_LINE_SEPARATOR, "---");
    }

    public static void populate24WeeksDates(ImmutableMap.Builder<String, String> builder) {
        LocalDate now = LocalDate.now();
        builder.put(PRACTICE_DIRECTION, now.format(ofPattern(D_MMM_YYYY)))
                .put(DAYS_14_FROM_DATE_OF_DIRECTION_KEY, now.plusDays(DAYS_14).format(ofPattern(D_MMM_YYYY)))
                .put(DAYS_42_FROM_DATE_OF_DIRECTION_KEY, now.plusDays(DAYS_42).format(ofPattern(D_MMM_YYYY)))
                .put(DAYS_56_FROM_DATE_OF_DIRECTION, now.plusDays(DAYS_56).format(ofPattern(D_MMM_YYYY)));
    }
}
