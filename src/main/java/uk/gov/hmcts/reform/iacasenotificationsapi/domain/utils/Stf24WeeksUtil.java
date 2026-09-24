package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.time.LocalDate;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

@Slf4j
public class Stf24WeeksUtil {
    public static final int DAYS_14 = 14;
    public static final int DAYS_42 = 42;
    public static final int DAYS_56 = 56;
    public static final String DAYS_14_FROM_DATE_OF_DIRECTION_KEY = "14DaysFromDateOfDirection";
    public static final String DAYS_56_FROM_DATE_OF_DIRECTION = "56DaysFromDateOfDirection";
    public static final String DAYS_42_FROM_DATE_OF_DIRECTION_KEY = "42DaysFromDateOfDirection";
    public static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL = "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL";
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

    public static final String STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_APPELLANT_EMAIL = "_STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_APPELLANT_EMAIL";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_EMAIL = "_STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_EMAIL";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_HOME_OFFICE_EMAIL = "_STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_HOME_OFFICE_EMAIL";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_LETTER = "_STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_LETTER";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_APPELLANT_LETTER = "_STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_APPELLANT_LETTER";

    public static final String STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_APPELLANT_LETTER = "_STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_APPELLANT_LETTER";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_LR_LETTER = "_STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_LR_LETTER";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL = "_STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL";



    public static final String WEEKS_DEADLINE = "24WeeksDeadline";
    public static final String DECISION_SENT_DATE = "decisionSentDate";
    public static final String PRACTICE_DIRECTION = "practiceDirection";
    public static final String APPEAL_RECEIVED_DATE = "appealReceivedDate";
    public static final String APPELLANT_FULL_NAME = "appellantFullName";
    public static final String EMPTY_STRING = "";
    public static final String HOME_OFFICE_REFERENCE_NUMBER_KEY = "homeOfficeReferenceNumber";
    public static final String LEGAL_REP_REFERENCE_NUMBER_KEY = "legalRepReferenceNumber";

    public static final String SUBJECT_PREFIX_KEY = "subjectPrefix";
    public static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";

    public static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    public static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    public static final String FYI_HEADING = "fyiHeading";
    public static final String FYI_TEXT = "fyiText";
    public static final String FYI_LINE_SEPARATOR = "fyiLineSeparator";
    public static final String LINK_TO_ONLINE_SERVICE_KEY = "linkToOnlineService";

    public static final String STF_24_WEEKS_HEARING_REQUIREMENTS_EMAIL_TEMPLATE = "${govnotify.template.submittedHearingRequirements24Weeks.email}";
    public static final String STF_24_WEEKS_HEARING_REQUIREMENTS_LETTER_TEMPLATE = "${govnotify.template.submittedHearingRequirements24Weeks.letter}";
    public static final String STF_24_WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_APPELLANT_LETTER_TEMPLATE = "${govnotify.template.uploadAdditionalEvidence24Weeks.letter}";
    public static final String STF_24_WEEKS_HEARING_REQ_HO_EMAIL_GENERATOR = "hearingRequirementsStatutoryTimeframe24WeeksHomeOfficeNotificationGenerator";
    public static final String STF_24_WEEKS_HEARING_REQ_APPELLANT_EMAIL_GENERATOR = "hearingRequirementsStatutoryTimeframe24WeeksAppellantNotificationGenerator";
    public static final String STF_24_WEEKS_HEARING_REQ_LR_EMAIL_GENERATOR = "hearingRequirementsStatutoryTimeframe24WeeksLegalRepresentativeNotificationGenerator";
    public static final String STF_24_WEEKS_HEARING_REQ_APPELLANT_LETTER_GENERATOR = "hearingRequirementsStatutoryTimeframe24WeeksAppellantLetterNotificationGenerator";
    public static final String STF_24_WEEKS_HEARING_REQ_LR_LETTER_GENERATOR = "hearingRequirementsStatutoryTimeframe24WeeksLegalRepresentativeLetterNotificationGenerator";
    public static final String STF_24_WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_APPELLANT_LETTER_GENERATOR = "uploadAdditionalEvidenceStf24WeeksAppellantLetterNotificationGenerator";
    public static final String STF_24_WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_LR_LETTER_GENERATOR = "uploadAdditionalEvidenceStf24WeeksLegalRepresentativeLetterNotificationGenerator";
    public static final String STF_24_WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL_GENERATOR = "uploadAdditionalEvidenceStf24WeeksHomeOfficeEmailNotificationGenerator";


    private static final String HO_REFERENCE_WITH_TEXT = "hoReferenceWithText";
    private static final String LR_REFERENCE_WITH_TEXT = "lrReferenceWithText";
    private static final String HEARING_DATE = "hearingDate";
    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";
    private static final String LEGAL_SUPPORT_INFO = "legalSupportInfo";

    public enum Stf24WeeksNotificationFor {
        LEGAL_REPRESENTATIVE, HOME_OFFICE, APPELLANT
    }

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

    public static ImmutableMap<String, String> buildHearingRequirementsLetterParameters(Stf24WeeksNotificationFor notificationFor, AsylumCase asylumCase, CustomerServicesProvider customerServicesProvider, DateTimeExtractor dateTimeExtractor, HearingDetailsFinder hearingDetailsFinder) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        ImmutableMap.Builder<String, String> builder = buildCommonParams(notificationFor, asylumCase, customerServicesProvider, dateTimeExtractor, hearingDetailsFinder);
        builder.put(LEGAL_SUPPORT_INFO, buildLegalSupportInfo(notificationFor));
        if (notificationFor == Stf24WeeksNotificationFor.LEGAL_REPRESENTATIVE) {
            buildAddressForLegalRepIccLetter(asylumCase, builder);
        } else if (notificationFor == Stf24WeeksNotificationFor.APPELLANT) {
            buildAddressForAppellantIccLetter(asylumCase, builder);
        }
        return builder.build();
    }

    public static ImmutableMap<String, String> buildHearingRequirementsEmailParams(Stf24WeeksNotificationFor notificationFor, AsylumCase asylumCase, String nonAdaPrefix, String iaExUiFrontendUrl, CustomerServicesProvider customerServicesProvider, DateTimeExtractor dateTimeExtractor, HearingDetailsFinder hearingDetailsFinder) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        ImmutableMap.Builder<String, String> builder = buildCommonParams(notificationFor, asylumCase, customerServicesProvider, dateTimeExtractor, hearingDetailsFinder)
                .put(SUBJECT_PREFIX_KEY, nonAdaPrefix).put(LINK_TO_ONLINE_SERVICE_KEY, iaExUiFrontendUrl);

        return builder.build();
    }

    private static ImmutableMap.@NonNull Builder<String, String> buildCommonParams(Stf24WeeksNotificationFor notificationFor, AsylumCase asylumCase, CustomerServicesProvider customerServicesProvider, DateTimeExtractor dateTimeExtractor, HearingDetailsFinder hearingDetailsFinder) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
                .put(APPELLANT_GIVEN_NAMES_KEY, asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING))
                .put(APPELLANT_FAMILY_NAME_KEY, asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING))
                .put(APPEAL_REFERENCE_NUMBER_KEY, asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put(HEARING_DATE, dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put(HEARING_CENTRE_ADDRESS, hearingDetailsFinder.getHearingCentreAddress(asylumCase));
        builder.putAll(PersonalisationProvider.getHearingRequirementsFields(asylumCase));
        builder.put(LR_REFERENCE_WITH_TEXT, legalRefText(notificationFor, asylumCase));
        builder.put(HO_REFERENCE_WITH_TEXT, hoRefText(notificationFor, asylumCase));
        return builder;

    }

    public static boolean is24WeeksCaseEvent(AsylumCase asylumCase, Event actualEvent, Event desiredEvent) {
        boolean hasStf24W = AsylumCaseUtils.hasStf24WeeksStatus(asylumCase);
        return Objects.equals(desiredEvent, actualEvent)
                && hasStf24W;
    }

    public static boolean canRunEventForNonInternalCase(PreSubmitCallbackStage callbackStage, Event event, AsylumCase asylumCase, boolean canRunFor24Weeks, Event event1) {
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && !isInternalCase(asylumCase)
                && canRunFor24Weeks
                && is24WeeksCaseEvent(asylumCase, event, event1);
    }

    public static boolean canRunEventForInternalCase(PreSubmitCallbackStage callbackStage, Event event, AsylumCase asylumCase, boolean canRunFor24Weeks, Event desiredEvent) {
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && canRunFor24Weeks
                && is24WeeksCaseEvent(asylumCase, event, desiredEvent);
    }


    private static String buildLegalSupportInfo(Stf24WeeksNotificationFor notificationFor) {

        String appellantInfo = "#How to find legal support and more information\n" +
                "\n" +
                "You may be able to find a solicitor or immigration adviser to help with your appeal. If you have access to a computer, you can:\n" +
                "\n" +
                "* search for a solicitor or immigration adviser\n" +
                "https://www.gov.uk/find-an-immigration-adviser\n" +
                "\n" +
                "* find out more about appealing an immigration or asylum decision\n" +
                "https://www.gov.uk/immigration-asylum-tribunal";
        if (notificationFor == Stf24WeeksNotificationFor.APPELLANT) {
            return appellantInfo;
        } else {
            return EMPTY_STRING;
        }
    }


    private static String legalRefText(Stf24WeeksNotificationFor notificationFor, AsylumCase asylumCase) {

        if (notificationFor == Stf24WeeksNotificationFor.LEGAL_REPRESENTATIVE) {
            String lrNumber = asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)
                    .filter(ref -> !ref.isEmpty())
                    .orElseGet(() -> asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class).orElse(EMPTY_STRING));
            return "Your reference:" + lrNumber;
        } else {
            return EMPTY_STRING;
        }
    }

    private static String hoRefText(Stf24WeeksNotificationFor notificationFor, AsylumCase asylumCase) {

        if (notificationFor == Stf24WeeksNotificationFor.HOME_OFFICE) {
            return "Home office reference:" + asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse("");
        } else {
            return EMPTY_STRING;
        }
    }


}
