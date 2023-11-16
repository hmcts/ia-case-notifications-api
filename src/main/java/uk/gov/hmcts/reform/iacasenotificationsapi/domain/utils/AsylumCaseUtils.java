package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

public class AsylumCaseUtils {

    private AsylumCaseUtils() {
        // prevent public constructor for Sonar
    }

    public static boolean isAppellantInDetention(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO).equals(YES);
    }

    public static boolean isAcceleratedDetainedAppeal(AsylumCase asylumCase) {
        return asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
            .orElse(NO)
            .equals(YES);
    }

    public static boolean isLegalRepEjp(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class).isPresent();
    }

    public static boolean isAgeAssessmentAppeal(AsylumCase asylumCase) {
        return (asylumCase.read(APPEAL_TYPE, AppealType.class)).orElse(null) == AppealType.AG;
    }

    public static boolean isInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_ADMIN, YesOrNo.class).map(isAdmin -> YES == isAdmin).orElse(false);
    }

    public static Optional<FtpaDecisionOutcomeType> getFtpaDecisionOutcomeType(AsylumCase asylumCase) {
        Optional<FtpaDecisionOutcomeType> ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);
        if (ftpaDecisionOutcomeType.isPresent()) {
            return ftpaDecisionOutcomeType;
        }
        return asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);
    }

    public static boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }

    public static String getDetentionFacilityName(AsylumCase asylumCase) {
        String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class)
                .orElse("");
        switch (detentionFacility) {
            case "immigrationRemovalCentre":
                return getFacilityName(IRC_NAME, asylumCase);
            case "prison":
                return getFacilityName(PRISON_NAME, asylumCase);
            case "other":
                return asylumCase.read(OTHER_DETENTION_FACILITY_NAME, OtherDetentionFacilityName.class)
                        .orElseThrow(() -> new RequiredFieldMissingException("Other detention facility name is missing")).getOther();
            default:
                throw new RequiredFieldMissingException("Detention Facility is missing");
        }
    }

    public static DocumentWithMetadata getLetterForNotification(AsylumCase asylumCase, DocumentTag documentTag) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalNotificationLetters = asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS);
        return optionalNotificationLetters
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(d -> d.getTag() == documentTag)
                .findFirst().orElseThrow(() -> new IllegalStateException(documentTag + " document not available"));
    }

    private static String getFacilityName(AsylumCaseDefinition field, AsylumCase asylumCase) {
        return asylumCase.read(field, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException(field.name() + " is missing"));
    }

    public static boolean isAipJourney(AsylumCase asylumCase) {

        return asylumCase
            .read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == AIP).orElse(false);
    }

    public static List<IdValue<DocumentWithMetadata>> getAddendumEvidenceDocuments(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingAdditionalEvidenceDocuments =
                asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS);
        if (maybeExistingAdditionalEvidenceDocuments.isEmpty()) {
            return Collections.emptyList();
        }

        return maybeExistingAdditionalEvidenceDocuments.get();
    }


    public static Optional<IdValue<DocumentWithMetadata>> getLatestAddendumEvidenceDocument(AsylumCase asylumCase) {
        List<IdValue<DocumentWithMetadata>> addendums = getAddendumEvidenceDocuments(asylumCase);

        if (addendums.isEmpty()) {
            return Optional.empty();
        }

        Optional<IdValue<DocumentWithMetadata>> optionalLatestAddendum = addendums.stream().findFirst();

        return optionalLatestAddendum.isEmpty() ? Optional.empty() : Optional.of(optionalLatestAddendum.get());
    }


    // This method uses the isEjp field which is set yes for EJP when a case is saved or no if paper form
    public static boolean isEjpCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_EJP, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES;
    }

    // This method checks who are the applicant to the costs order
    public static boolean isHomeOfficeApplicant(AsylumCase asylumCase) {
        ApplyForCosts latestApplyForCosts = retrieveLatestApplyForCosts(asylumCase).getValue();
        final String applicantType = latestApplyForCosts.getApplyForCostsApplicantType();
        if (applicantType.equals("Home office")) {
            return true;
        } else if (applicantType.equals("Legal representative")) {
            return false;
        }
        throw new IllegalStateException("Correct applicant type is not present");
    }

    public static IdValue<ApplyForCosts> retrieveLatestApplyForCosts(AsylumCase asylumCase) {
        Optional<List<IdValue<ApplyForCosts>>> applyForCosts = asylumCase.read(APPLIES_FOR_COSTS);

        if (applyForCosts.isPresent()) {
            List<IdValue<ApplyForCosts>> applyForCostsList = applyForCosts.get();
            return applyForCostsList.get(0);
        } else {
            throw new IllegalStateException("Applies for costs are not present");
        }
    }

}
