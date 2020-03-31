package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@Service
public class PersonalisationProvider {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";
    private final String iaExUiFrontendUrl;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final DirectionFinder directionFinder;
    private final DateTimeExtractor dateTimeExtractor;

    ImmutableMap.Builder<String, AsylumCaseDefinition> personalisationBuilder = new ImmutableMap.Builder<String, AsylumCaseDefinition>()
        .put("appealReferenceNumber", APPEAL_REFERENCE_NUMBER)
        .put("legalRepReferenceNumber", LEGAL_REP_REFERENCE_NUMBER)
        .put("ariaListingReference", ARIA_LISTING_REFERENCE)
        .put("homeOfficeReferenceNumber", HOME_OFFICE_REFERENCE_NUMBER)
        .put("appellantGivenNames", APPELLANT_GIVEN_NAMES)
        .put("appellantFamilyName", APPELLANT_FAMILY_NAME);

    Map<Event, Map<String, AsylumCaseDefinition>> eventDefinition = new ImmutableMap.Builder<Event, Map<String, AsylumCaseDefinition>>()
        .put(CHANGE_DIRECTION_DUE_DATE, personalisationBuilder
            .build())
        .put(DRAFT_HEARING_REQUIREMENTS, personalisationBuilder
            .build())
        .put(EDIT_CASE_LISTING, personalisationBuilder
            .build())
        .put(UPLOAD_ADDITIONAL_EVIDENCE, personalisationBuilder
            .build())
        .put(UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE, personalisationBuilder
            .build())
        .put(UPLOAD_ADDENDUM_EVIDENCE, personalisationBuilder
            .build())
        .put(UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP, personalisationBuilder
            .build())
        .put(UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE, personalisationBuilder
            .build())
        .put(SEND_DIRECTION, personalisationBuilder
            .build())
        .put(APPLY_FOR_FTPA_APPELLANT, personalisationBuilder
            .build())
        .put(APPLY_FOR_FTPA_RESPONDENT, personalisationBuilder
            .build())
        .build();

    public PersonalisationProvider(
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        HearingDetailsFinder hearingDetailsFinder,
        DirectionFinder directionFinder,
        DateTimeExtractor dateTimeExtractor) {
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.directionFinder = directionFinder;
        this.dateTimeExtractor = dateTimeExtractor;
    }

    public Map<String, String> getEditCaseListingPersonalisation(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        final String hearingCentreAddress =
            hearingDetailsFinder.getHearingCentreAddress(asylumCase);

        final String hearingDateTime =
            hearingDetailsFinder.getHearingDateTime(asylumCase);

        String hearingCentreNameBefore = "";
        String oldHearingDate = "";

        if (caseDetailsBefore.isPresent()) {

            hearingCentreNameBefore =
                hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.get().getCaseData());

            oldHearingDate =
                hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.get().getCaseData());
        }

        final Builder<String, String> caseListingValues = ImmutableMap
            .<String, String>builder()
            .put("Hyperlink to user’s case list", iaExUiFrontendUrl)
            .put("oldHearingCentre", hearingCentreNameBefore)
            .put("oldHearingDate", oldHearingDate == null || oldHearingDate.isEmpty() ? "" : dateTimeExtractor.extractHearingDate(oldHearingDate))
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDateTime))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDateTime))
            .put(HEARING_CENTRE_ADDRESS, hearingCentreAddress);

        buildHearingRequirementsFields(asylumCase, caseListingValues);

        return caseListingValues.build();
    }

    public static void buildHearingRequirementsFields(AsylumCase asylumCase, Builder<String, String> caseListingValues) {

        final Optional<YesOrNo> isSubmitRequirementsAvailable = asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE);

        if (isSubmitRequirementsAvailable.isPresent() && isSubmitRequirementsAvailable.get() == YesOrNo.YES) {

            caseListingValues
                .put("Hearing Requirement Vulnerabilities", readStringCaseField(asylumCase, VULNERABILITIES_TRIBUNAL_RESPONSE,
                    "No special adjustments are being made to accommodate vulnerabilities"))
                .put("Hearing Requirement Multimedia", readStringCaseField(asylumCase, MULTIMEDIA_TRIBUNAL_RESPONSE,
                    "No multimedia equipment is being provided"))
                .put("Hearing Requirement Single Sex Court", readStringCaseField(asylumCase, SINGLE_SEX_COURT_TRIBUNAL_RESPONSE,
                    "The court will not be single sex"))
                .put("Hearing Requirement In Camera Court", readStringCaseField(asylumCase, IN_CAMERA_COURT_TRIBUNAL_RESPONSE,
                    "The hearing will be held in public court"))
                .put("Hearing Requirement Other", readStringCaseField(asylumCase, ADDITIONAL_TRIBUNAL_RESPONSE,
                    "No other adjustments are being made"));
        } else {

            caseListingValues
                .put("Hearing Requirement Vulnerabilities", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_VULNERABILITIES,
                    "No special adjustments are being made to accommodate vulnerabilities"))
                .put("Hearing Requirement Multimedia", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_MULTIMEDIA,
                    "No multimedia equipment is being provided"))
                .put("Hearing Requirement Single Sex Court", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT,
                    "The court will not be single sex"))
                .put("Hearing Requirement In Camera Court", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT,
                    "The hearing will be held in public court"))
                .put("Hearing Requirement Other", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_OTHER,
                    "No other adjustments are being made"));
        }
    }

    public Map<String, String> getNonStandardDirectionPersonalisation(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final Direction direction = directionFinder
            .findFirst(asylumCase, DirectionTag.NONE)
            .orElseThrow(() -> new IllegalStateException("non-standard direction is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .put("iaCaseListHyperLink", iaExUiFrontendUrl)
            .put("explanation", direction.getExplanation())
            .put("dueDate", directionDueDate)
            .build();

    }

    public Map<String, String> getChangeDirectionDueDatePersonalisation(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String directionEditDueDate = asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)
            .orElseThrow(() -> new IllegalStateException("Direction edit date due is not present"));

        String directionEditExplanation = asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class)
            .orElseThrow(() -> new IllegalStateException("Direction edit explanation is not present"));

        final String directionDueDate =
            LocalDate
                .parse(directionEditDueDate)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .put("iaCaseListHyperLink", iaExUiFrontendUrl)
            .put("explanation", directionEditExplanation)
            .put("dueDate", directionDueDate)
            .build();
    }

    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Map<String, String> immutableMap = eventDefinition
            .get(callback.getEvent())
            .entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> asylumCase.read(e.getValue(), String.class).orElse("N/A")));

        if (callback.getEvent() == Event.SEND_DIRECTION) {
            immutableMap.putAll(getNonStandardDirectionPersonalisation(callback));
        } else if (callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE) {
            immutableMap.putAll(getChangeDirectionDueDatePersonalisation(callback));
        } else if (callback.getEvent() == Event.EDIT_CASE_LISTING) {
            immutableMap.putAll(getEditCaseListingPersonalisation(callback));
        }

        return immutableMap;
    }

    public Map<String, String> getReviewedHearingRequirementsPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }

    private static String readStringCaseField(final AsylumCase asylumCase, final AsylumCaseDefinition caseField, final String defaultIfNotPresent) {

        final Optional<String> optionalFieldValue = asylumCase.read(caseField, String.class);
        return optionalFieldValue.isPresent() && !optionalFieldValue.get().isEmpty() ? optionalFieldValue.get() : defaultIfNotPresent;
    }

    public String getFtpaDecisionTemplateId(AsylumCase asylumCase,
                                            GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration) {
        FtpaAppellantDecisionOutcomeType ftpaAppellantDecisionOutcomeType = asylumCase
            .read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaAppellantDecisionOutcomeType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicationDecision is not present"));

        YesOrNo applicant = asylumCase
            .read(FTPA_RESPONDENT_SUBMITTED, YesOrNo.class)
            .orElseThrow(() -> new IllegalStateException("ftpaSubmittedApplicant is not present"));

        if (ftpaAppellantDecisionOutcomeType.toString().equals(FtpaAppellantDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return applicant.equals(YesOrNo.YES) ? govNotifyTemplateIdConfiguration.getApplicationGrantedApplicant() : govNotifyTemplateIdConfiguration.getApplicationGrantedOtherParty();
        } else if (ftpaAppellantDecisionOutcomeType.toString().equals(FtpaAppellantDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString())) {
            return applicant.equals(YesOrNo.YES) ? govNotifyTemplateIdConfiguration.getApplicationPartiallyGrantedApplicant() : govNotifyTemplateIdConfiguration.getApplicationPartiallyGrantedOtherParty();
        } else if (ftpaAppellantDecisionOutcomeType.toString().equals(FtpaAppellantDecisionOutcomeType.FTPA_REFUSED.toString())) {
            return applicant.equals(YesOrNo.YES) ? govNotifyTemplateIdConfiguration.getApplicationRefusedApplicant() : govNotifyTemplateIdConfiguration.getApplicationRefusedOtherParty();
        } else {
            return applicant.equals(YesOrNo.YES) ? govNotifyTemplateIdConfiguration.getApplicationNotAdmittedApplicant() : govNotifyTemplateIdConfiguration.getApplicationNotAdmittedOtherParty();
        }
    }

    public Map<String, String> getFtpaDecisionPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }
}
