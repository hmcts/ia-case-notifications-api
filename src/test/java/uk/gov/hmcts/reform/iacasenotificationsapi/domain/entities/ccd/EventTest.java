package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventTest {

    private static final String[] UNIQUE_EVENTS = {
        "addAppealResponse", "addEvidenceForCosts", "adjournHearingWithoutDate", "adaSuitabilityReview",
        "applyForCosts", "applyForFTPAAppellant", "applyForFTPARespondent", "asyncStitchingComplete",
        "buildCase", "caseListing", "changeBailDirectionDueDate", "changeDirectionDueDate",
        "changeHearingCentre", "considerMakingCostsOrder", "createBailCaseLink", "createCaseLink",
        "createCaseSummary", "customiseHearingBundle", "decideAnApplication", "decideCostsApplication",
        "decideFtpaApplication", "decisionWithoutHearing", "draftHearingRequirements", "editAppeal",
        "editAppealAfterSubmit", "editBailApplicationAfterSubmit", "editBailDocuments", "editCaseListing",
        "editDocuments", "editPaymentMethod", "endAppeal", "endAppealAutomatically", "endApplication",
        "forceCaseToCaseUnderReview", "forceCaseToHearing", "forceCaseToSubmitHearingRequirements",
        "forceRequestCaseBuilding", "generateHearingBundle", "leadershipJudgeFtpaDecision", "linkAppeal",
        "listCase", "listCaseWithoutHearingRequirements", "listCma", "makeAnApplication", "makeNewApplication",
        "manageFeeUpdate", "markAppealAsAda", "markAppealAsDetained", "markAppealPaid", "markAsReadyForUtTransfer",
        "maintainBailCaseLinks", "maintainCaseLinks", "nocRequest", "nocRequestBail", "payAndSubmitAppeal",
        "payForAppeal", "paymentAppeal", "recordApplication", "recordOutOfTimeDecision", "recordRemissionDecision",
        "recordTheDecision", "reinstateAppeal", "removeAppealFromOnline", "removeDetainedStatus",
        "removeLegalRepresentative", "removeRepresentation", "respondToCosts", "requestCaseBuilding",
        "requestCaseEdit", "requestCmaRequirements", "requestFeeRemission", "requestHearingRequirements",
        "requestHearingRequirementsFeature", "requestNewHearingRequirements", "requestReasonsForAppeal",
        "requestRespondentEvidence", "requestRespondentReview", "requestResponseAmend", "requestResponseReview",
        "residentJudgeFtpaDecision", "restoreStateFromAdjourn", "revertStateToAwaitingRespondentEvidence",
        "reviewHearingRequirements", "reviewTimeExtension", "sendBailDirection", "sendDecisionAndReasons",
        "sendDirection", "sendDirectionWithQuestions", "sendUploadBailSummaryDirection", "startAppeal",
        "stopLegalRepresenting", "submitAddendumEvidence", "submitAddendumEvidenceAdminOfficer",
        "submitAddendumEvidenceHomeOffice", "submitAddendumEvidenceLegalRep", "submitAppeal", "submitApplication",
        "submitCase", "submitClarifyingQuestionAnswers", "submitCmaRequirements", "submitReasonsForAppeal",
        "submitTimeExtension", "transferOutOfAda", "turnOnNotifications", "unlinkAppeal", "updateDetentionLocation",
        "updateHearingAdjustments", "updatePaymentStatus", "updateTribunalDecision", "uploadAddendumEvidence",
        "uploadAddendumEvidenceAdminOfficer", "uploadAddendumEvidenceHomeOffice", "uploadAddendumEvidenceLegalRep",
        "uploadAdditionalEvidence", "uploadAdditionalEvidenceHomeOffice", "uploadBailSummary", "uploadDocuments",
        "uploadHomeOfficeAppealResponse", "uploadHomeOfficeBundle", "uploadRespondentEvidence", "uploadSignedDecisionNotice",
        "unknown"
    };

    @ParameterizedTest
    @MethodSource("eventValuesProvider")
    void enum_events_have_correct_values(String expected, Event event) {
        assertEquals(expected, String.valueOf(event));
    }

    static Stream<Arguments> eventValuesProvider() {
        return Stream.of(
                Arguments.of("addAppealResponse", Event.ADD_APPEAL_RESPONSE),
                Arguments.of("addEvidenceForCosts", Event.ADD_EVIDENCE_FOR_COSTS),
                Arguments.of("adjournHearingWithoutDate", Event.ADJOURN_HEARING_WITHOUT_DATE),
                Arguments.of("adaSuitabilityReview", Event.ADA_SUITABILITY_REVIEW),
                Arguments.of("applyForCosts", Event.APPLY_FOR_COSTS),
                Arguments.of("applyForFTPAAppellant", Event.APPLY_FOR_FTPA_APPELLANT),
                Arguments.of("applyForFTPARespondent", Event.APPLY_FOR_FTPA_RESPONDENT),
                Arguments.of("asyncStitchingComplete", Event.ASYNC_STITCHING_COMPLETE),
                Arguments.of("buildCase", Event.BUILD_CASE),
                Arguments.of("caseListing", Event.CASE_LISTING),
                Arguments.of("changeBailDirectionDueDate", Event.CHANGE_BAIL_DIRECTION_DUE_DATE),
                Arguments.of("changeDirectionDueDate", Event.CHANGE_DIRECTION_DUE_DATE),
                Arguments.of("changeHearingCentre", Event.CHANGE_HEARING_CENTRE),
                Arguments.of("considerMakingCostsOrder", Event.CONSIDER_MAKING_COSTS_ORDER),
                Arguments.of("createBailCaseLink", Event.CREATE_BAIL_CASE_LINK),
                Arguments.of("createCaseLink", Event.CREATE_CASE_LINK),
                Arguments.of("createCaseSummary", Event.CREATE_CASE_SUMMARY),
                Arguments.of("customiseHearingBundle", Event.CUSTOMISE_HEARING_BUNDLE),
                Arguments.of("decideAnApplication", Event.DECIDE_AN_APPLICATION),
                Arguments.of("decideCostsApplication", Event.DECIDE_COSTS_APPLICATION),
                Arguments.of("decideFtpaApplication", Event.DECIDE_FTPA_APPLICATION),
                Arguments.of("decisionWithoutHearing", Event.DECISION_WITHOUT_HEARING),
                Arguments.of("draftHearingRequirements", Event.DRAFT_HEARING_REQUIREMENTS),
                Arguments.of("editAppeal", Event.EDIT_APPEAL),
                Arguments.of("editAppealAfterSubmit", Event.EDIT_APPEAL_AFTER_SUBMIT),
                Arguments.of("editBailApplicationAfterSubmit", Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT),
                Arguments.of("editBailDocuments", Event.EDIT_BAIL_DOCUMENTS),
                Arguments.of("editCaseListing", Event.EDIT_CASE_LISTING),
                Arguments.of("editDocuments", Event.EDIT_DOCUMENTS),
                Arguments.of("editPaymentMethod", Event.EDIT_PAYMENT_METHOD),
                Arguments.of("endAppeal", Event.END_APPEAL),
                Arguments.of("endAppealAutomatically", Event.END_APPEAL_AUTOMATICALLY),
                Arguments.of("endApplication", Event.END_APPLICATION),
                Arguments.of("forceCaseToCaseUnderReview", Event.FORCE_CASE_TO_CASE_UNDER_REVIEW),
                Arguments.of("forceCaseToHearing", Event.FORCE_CASE_TO_HEARING),
                Arguments.of("forceCaseToSubmitHearingRequirements", Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS),
                Arguments.of("forceRequestCaseBuilding", Event.FORCE_REQUEST_CASE_BUILDING),
                Arguments.of("generateHearingBundle", Event.GENERATE_HEARING_BUNDLE),
                Arguments.of("leadershipJudgeFtpaDecision", Event.LEADERSHIP_JUDGE_FTPA_DECISION),
                Arguments.of("linkAppeal", Event.LINK_APPEAL),
                Arguments.of("listCase", Event.LIST_CASE),
                Arguments.of("listCaseWithoutHearingRequirements", Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS),
                Arguments.of("listCma", Event.LIST_CMA),
                Arguments.of("makeAnApplication", Event.MAKE_AN_APPLICATION),
                Arguments.of("makeNewApplication", Event.MAKE_NEW_APPLICATION),
                Arguments.of("manageFeeUpdate", Event.MANAGE_FEE_UPDATE),
                Arguments.of("markAppealAsAda", Event.MARK_APPEAL_AS_ADA),
                Arguments.of("markAppealAsDetained", Event.MARK_APPEAL_AS_DETAINED),
                Arguments.of("markAppealPaid", Event.MARK_APPEAL_PAID),
                Arguments.of("markAppealRemitted", Event.MARK_APPEAL_AS_REMITTED),
                Arguments.of("markAsReadyForUtTransfer", Event.MARK_AS_READY_FOR_UT_TRANSFER),
                Arguments.of("maintainBailCaseLinks", Event.MAINTAIN_BAIL_CASE_LINKS),
                Arguments.of("maintainCaseLinks", Event.MAINTAIN_CASE_LINKS),
                Arguments.of("nocRequest", Event.NOC_REQUEST),
                Arguments.of("nocRequestBail", Event.NOC_REQUEST_BAIL),
                Arguments.of("payAndSubmitAppeal", Event.PAY_AND_SUBMIT_APPEAL),
                Arguments.of("payForAppeal", Event.PAY_FOR_APPEAL),
                Arguments.of("paymentAppeal", Event.PAYMENT_APPEAL),
                Arguments.of("recordApplication", Event.RECORD_APPLICATION),
                Arguments.of("recordOutOfTimeDecision", Event.RECORD_OUT_OF_TIME_DECISION),
                Arguments.of("recordRemissionDecision", Event.RECORD_REMISSION_DECISION),
                Arguments.of("recordTheDecision", Event.RECORD_THE_DECISION),
                Arguments.of("reinstateAppeal", Event.REINSTATE_APPEAL),
                Arguments.of("removeAppealFromOnline", Event.REMOVE_APPEAL_FROM_ONLINE),
                Arguments.of("removeDetainedStatus", Event.REMOVE_DETAINED_STATUS),
                Arguments.of("removeLegalRepresentative", Event.REMOVE_LEGAL_REPRESENTATIVE),
                Arguments.of("removeRepresentation", Event.REMOVE_REPRESENTATION),
                Arguments.of("respondToCosts", Event.RESPOND_TO_COSTS),
                Arguments.of("requestCaseBuilding", Event.REQUEST_CASE_BUILDING),
                Arguments.of("requestCaseEdit", Event.REQUEST_CASE_EDIT),
                Arguments.of("requestCmaRequirements", Event.REQUEST_CMA_REQUIREMENTS),
                Arguments.of("requestFeeRemission", Event.REQUEST_FEE_REMISSION),
                Arguments.of("requestHearingRequirements", Event.REQUEST_HEARING_REQUIREMENTS),
                Arguments.of("requestHearingRequirementsFeature", Event.REQUEST_HEARING_REQUIREMENTS_FEATURE),
                Arguments.of("requestNewHearingRequirements", Event.REQUEST_NEW_HEARING_REQUIREMENTS),
                Arguments.of("requestReasonsForAppeal", Event.REQUEST_REASONS_FOR_APPEAL),
                Arguments.of("requestRespondentEvidence", Event.REQUEST_RESPONDENT_EVIDENCE),
                Arguments.of("requestRespondentReview", Event.REQUEST_RESPONDENT_REVIEW),
                Arguments.of("requestResponseAmend", Event.REQUEST_RESPONSE_AMEND),
                Arguments.of("requestResponseReview", Event.REQUEST_RESPONSE_REVIEW),
                Arguments.of("residentJudgeFtpaDecision", Event.RESIDENT_JUDGE_FTPA_DECISION),
                Arguments.of("restoreStateFromAdjourn", Event.RESTORE_STATE_FROM_ADJOURN),
                Arguments.of("revertStateToAwaitingRespondentEvidence", Event.REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE),
                Arguments.of("reviewHearingRequirements", Event.REVIEW_HEARING_REQUIREMENTS),
                Arguments.of("reviewTimeExtension", Event.REVIEW_TIME_EXTENSION),
                Arguments.of("sendBailDirection", Event.SEND_BAIL_DIRECTION),
                Arguments.of("sendDecisionAndReasons", Event.SEND_DECISION_AND_REASONS),
                Arguments.of("sendDirection", Event.SEND_DIRECTION),
                Arguments.of("sendDirectionWithQuestions", Event.SEND_DIRECTION_WITH_QUESTIONS),
                Arguments.of("sendUploadBailSummaryDirection", Event.SEND_UPLOAD_BAIL_SUMMARY_DIRECTION),
                Arguments.of("startAppeal", Event.START_APPEAL),
                Arguments.of("stopLegalRepresenting", Event.STOP_LEGAL_REPRESENTING),
                Arguments.of("submitAddendumEvidence", Event.SUBMIT_ADDENDUM_EVIDENCE),
                Arguments.of("submitAddendumEvidenceAdminOfficer", Event.SUBMIT_ADDENDUM_EVIDENCE_ADMIN_OFFICER),
                Arguments.of("submitAddendumEvidenceHomeOffice", Event.SUBMIT_ADDENDUM_EVIDENCE_HOME_OFFICE),
                Arguments.of("submitAddendumEvidenceLegalRep", Event.SUBMIT_ADDENDUM_EVIDENCE_LEGAL_REP),
                Arguments.of("submitAppeal", Event.SUBMIT_APPEAL),
                Arguments.of("submitApplication", Event.SUBMIT_APPLICATION),
                Arguments.of("submitCase", Event.SUBMIT_CASE),
                Arguments.of("submitClarifyingQuestionAnswers", Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS),
                Arguments.of("submitCmaRequirements", Event.SUBMIT_CMA_REQUIREMENTS),
                Arguments.of("submitReasonsForAppeal", Event.SUBMIT_REASONS_FOR_APPEAL),
                Arguments.of("submitTimeExtension", Event.SUBMIT_TIME_EXTENSION),
                Arguments.of("transferOutOfAda", Event.TRANSFER_OUT_OF_ADA),
                Arguments.of("turnOnNotifications", Event.TURN_ON_NOTIFICATIONS),
                Arguments.of("unlinkAppeal", Event.UNLINK_APPEAL),
                Arguments.of("updateDetentionLocation", Event.UPDATE_DETENTION_LOCATION),
                Arguments.of("updateHearingAdjustments", Event.UPDATE_HEARING_ADJUSTMENTS),
                Arguments.of("updatePaymentStatus", Event.UPDATE_PAYMENT_STATUS),
                Arguments.of("updateTribunalDecision", Event.UPDATE_TRIBUNAL_DECISION),
                Arguments.of("uploadAddendumEvidence", Event.UPLOAD_ADDENDUM_EVIDENCE),
                Arguments.of("uploadAddendumEvidenceAdminOfficer", Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER),
                Arguments.of("uploadAddendumEvidenceHomeOffice", Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE),
                Arguments.of("uploadAddendumEvidenceLegalRep", Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP),
                Arguments.of("uploadAdditionalEvidence", Event.UPLOAD_ADDITIONAL_EVIDENCE),
                Arguments.of("uploadAdditionalEvidenceHomeOffice", Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE),
                Arguments.of("uploadBailSummary", Event.UPLOAD_BAIL_SUMMARY),
                Arguments.of("uploadDocuments", Event.UPLOAD_DOCUMENTS),
                Arguments.of("uploadHomeOfficeAppealResponse", Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE),
                Arguments.of("uploadHomeOfficeBundle", Event.UPLOAD_HOME_OFFICE_BUNDLE),
                Arguments.of("uploadRespondentEvidence", Event.UPLOAD_RESPONDENT_EVIDENCE),
                Arguments.of("uploadSignedDecisionNotice", Event.UPLOAD_SIGNED_DECISION_NOTICE),
                Arguments.of("unknown", Event.UNKNOWN)
        );
    }

    @Test
    public void event_enum_should_contain_following_events() {
        List<String> enumEvents = Arrays.stream(Event.values())
                .map(Enum::toString)
                .toList();

        List<String> testedEvents = List.of(
                UNIQUE_EVENTS
        );

        List<String> missingEvents = findMissingEvents(enumEvents, testedEvents);

        List<String> extraEvents = findMissingEvents(testedEvents, enumEvents);

        assertTrue(missingEvents.isEmpty(), "Missing the following enum events in this test: " + String.join(", ", missingEvents));
        assertTrue(extraEvents.isEmpty(), "Extra events in this test not found in enum: " + String.join(", ", extraEvents));
    }

    @NotNull
    private static List<String> findMissingEvents(List<String> enumEvents, List<String> testedEvents) {
        return enumEvents.stream()
                .filter(e -> !testedEvents.contains(e))
                .collect(Collectors.toList());
    }
}
