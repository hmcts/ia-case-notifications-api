package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
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

    public static boolean isDetainedAppeal(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)
                .orElse(NO)
                .equals(YES);
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
}
