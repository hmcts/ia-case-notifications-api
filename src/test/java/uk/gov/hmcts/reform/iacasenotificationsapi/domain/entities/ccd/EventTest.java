package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

public class EventTest {

    @ParameterizedTest
    @MethodSource("eventMapping")
    void has_correct_values(String expected, String actual) {
        assertEquals(expected, actual);
    }

    @Test
    void if_this_test_fails_it_is_because_eventMapping_needs_updating_with_your_changes() {
        List<String> eventMappingStrings = eventMapping().map(arg -> arg.get()[1])
            .map(String.class::cast)
            .toList();
        List<Event> missingEvents = Arrays.stream(Event.values())
            .filter(event -> !eventMappingStrings.contains(event.toString())).toList();
        assertTrue(missingEvents.isEmpty(), "The following events are missing from the eventMapping method: " + missingEvents);
    }

    static Stream<Arguments> eventMapping() {
        return Stream.of(
            Arguments.of("startAppeal", Event.START_APPEAL.toString()),
            Arguments.of("editAppeal", Event.EDIT_APPEAL.toString()),
            Arguments.of("submitAppeal", Event.SUBMIT_APPEAL.toString()),
            Arguments.of("payAndSubmitAppeal", Event.PAY_AND_SUBMIT_APPEAL.toString()),
            Arguments.of("payForAppeal", Event.PAY_FOR_APPEAL.toString()),
            Arguments.of("sendDirection", Event.SEND_DIRECTION.toString()),
            Arguments.of("uploadHomeOfficeBundle", Event.UPLOAD_HOME_OFFICE_BUNDLE.toString()),
            Arguments.of("removeDetainedStatus", Event.REMOVE_DETAINED_STATUS.toString()),
            Arguments.of("markAppealAsDetained", Event.MARK_APPEAL_AS_DETAINED.toString()),
            Arguments.of("sendUploadBailSummaryDirection", Event.SEND_UPLOAD_BAIL_SUMMARY_DIRECTION.toString()),
            Arguments.of("forceCaseToHearing", Event.FORCE_CASE_TO_HEARING.toString()),
            Arguments.of("startApplication", Event.START_APPLICATION.toString()),
            Arguments.of("editBailApplication", Event.EDIT_BAIL_APPLICATION.toString()),
            Arguments.of("requestRespondentEvidence", Event.REQUEST_RESPONDENT_EVIDENCE.toString()),
            Arguments.of("uploadRespondentEvidence", Event.UPLOAD_RESPONDENT_EVIDENCE.toString()),
            Arguments.of("buildCase", Event.BUILD_CASE.toString()),
            Arguments.of("submitCase", Event.SUBMIT_CASE.toString()),
            Arguments.of("requestCaseEdit", Event.REQUEST_CASE_EDIT.toString()),
            Arguments.of("requestRespondentReview", Event.REQUEST_RESPONDENT_REVIEW.toString()),
            Arguments.of("addAppealResponse", Event.ADD_APPEAL_RESPONSE.toString()),
            Arguments.of("requestHearingRequirements", Event.REQUEST_HEARING_REQUIREMENTS.toString()),
            Arguments.of("reviewHearingRequirements", Event.REVIEW_HEARING_REQUIREMENTS.toString()),
            Arguments.of("listCaseWithoutHearingRequirements", Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS.toString()),
            Arguments.of("requestHearingRequirementsFeature", Event.REQUEST_HEARING_REQUIREMENTS_FEATURE.toString()),
            Arguments.of("draftHearingRequirements", Event.DRAFT_HEARING_REQUIREMENTS.toString()),
            Arguments.of("changeDirectionDueDate", Event.CHANGE_DIRECTION_DUE_DATE.toString()),
            Arguments.of("uploadAdditionalEvidence", Event.UPLOAD_ADDITIONAL_EVIDENCE.toString()),
            Arguments.of("uploadAdditionalEvidenceHomeOffice", Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE.toString()),
            Arguments.of("listCase", Event.LIST_CASE.toString()),
            Arguments.of("createCaseSummary", Event.CREATE_CASE_SUMMARY.toString()),
            Arguments.of("revertStateToAwaitingRespondentEvidence", Event.REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE.toString()),
            Arguments.of("generateHearingBundle", Event.GENERATE_HEARING_BUNDLE.toString()),
            Arguments.of("asyncStitchingComplete", Event.ASYNC_STITCHING_COMPLETE.toString()),
            Arguments.of("customiseHearingBundle", CUSTOMISE_HEARING_BUNDLE.toString()),
            Arguments.of("editCaseListing", Event.EDIT_CASE_LISTING.toString()),
            Arguments.of("endAppeal", Event.END_APPEAL.toString()),
            Arguments.of("recordApplication", Event.RECORD_APPLICATION.toString()),
            Arguments.of("requestCaseBuilding", Event.REQUEST_CASE_BUILDING.toString()),
            Arguments.of("forceRequestCaseBuilding", Event.FORCE_REQUEST_CASE_BUILDING.toString()),
            Arguments.of("uploadHomeOfficeAppealResponse", Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE.toString()),
            Arguments.of("uploadAddendumEvidence", Event.UPLOAD_ADDENDUM_EVIDENCE.toString()),
            Arguments.of("uploadAddendumEvidenceLegalRep", Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP.toString()),
            Arguments.of("uploadAddendumEvidenceHomeOffice", Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE.toString()),
            Arguments.of("uploadAddendumEvidenceAdminOfficer", Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER.toString()),
            Arguments.of("requestResponseReview", Event.REQUEST_RESPONSE_REVIEW.toString()),
            Arguments.of("decisionWithoutHearing", Event.DECISION_WITHOUT_HEARING.toString()),
            Arguments.of("sendDecisionAndReasons", Event.SEND_DECISION_AND_REASONS.toString()),
            Arguments.of("requestReasonsForAppeal", Event.REQUEST_REASONS_FOR_APPEAL.toString()),
            Arguments.of("submitReasonsForAppeal", Event.SUBMIT_REASONS_FOR_APPEAL.toString()),
            Arguments.of("updateHearingAdjustments", Event.UPDATE_HEARING_ADJUSTMENTS.toString()),
            Arguments.of("removeAppealFromOnline", Event.REMOVE_APPEAL_FROM_ONLINE.toString()),
            Arguments.of("changeHearingCentre", Event.CHANGE_HEARING_CENTRE.toString()),
            Arguments.of("applyForFTPAAppellant", Event.APPLY_FOR_FTPA_APPELLANT.toString()),
            Arguments.of("applyForFTPARespondent", Event.APPLY_FOR_FTPA_RESPONDENT.toString()),
            Arguments.of("submitTimeExtension", Event.SUBMIT_TIME_EXTENSION.toString()),
            Arguments.of("reviewTimeExtension", Event.REVIEW_TIME_EXTENSION.toString()),
            Arguments.of("sendDirectionWithQuestions", Event.SEND_DIRECTION_WITH_QUESTIONS.toString()),
            Arguments.of("submitClarifyingQuestionAnswers", Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS.toString()),
            Arguments.of("forceCaseToCaseUnderReview", Event.FORCE_CASE_TO_CASE_UNDER_REVIEW.toString()),
            Arguments.of("forceCaseToSubmitHearingRequirements", Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS.toString()),
            Arguments.of("adjournHearingWithoutDate", Event.ADJOURN_HEARING_WITHOUT_DATE.toString()),
            Arguments.of("recordAdjournmentDetails", Event.RECORD_ADJOURNMENT_DETAILS.toString()),
            Arguments.of("restoreStateFromAdjourn", Event.RESTORE_STATE_FROM_ADJOURN.toString()),
            Arguments.of("requestCmaRequirements", Event.REQUEST_CMA_REQUIREMENTS.toString()),
            Arguments.of("submitCmaRequirements", Event.SUBMIT_CMA_REQUIREMENTS.toString()),
            Arguments.of("listCma", Event.LIST_CMA.toString()),
            Arguments.of("editAppealAfterSubmit", Event.EDIT_APPEAL_AFTER_SUBMIT.toString()),
            Arguments.of("linkAppeal", Event.LINK_APPEAL.toString()),
            Arguments.of("unlinkAppeal", Event.UNLINK_APPEAL.toString()),
            Arguments.of("leadershipJudgeFtpaDecision", Event.LEADERSHIP_JUDGE_FTPA_DECISION.toString()),
            Arguments.of("residentJudgeFtpaDecision", Event.RESIDENT_JUDGE_FTPA_DECISION.toString()),
            Arguments.of("editDocuments", Event.EDIT_DOCUMENTS.toString()),
            Arguments.of("paymentAppeal", Event.PAYMENT_APPEAL.toString()),
            Arguments.of("requestResponseAmend", Event.REQUEST_RESPONSE_AMEND.toString()),
            Arguments.of("markAppealPaid", Event.MARK_APPEAL_PAID.toString()),
            Arguments.of("reinstateAppeal", REINSTATE_APPEAL.toString()),
            Arguments.of("makeAnApplication", Event.MAKE_AN_APPLICATION.toString()),
            Arguments.of("decideAnApplication", Event.DECIDE_AN_APPLICATION.toString()),
            Arguments.of("requestNewHearingRequirements", Event.REQUEST_NEW_HEARING_REQUIREMENTS.toString()),
            Arguments.of("recordRemissionDecision", RECORD_REMISSION_DECISION.toString()),
            Arguments.of("nocRequest", NOC_REQUEST.toString()),
            Arguments.of("removeRepresentation", REMOVE_REPRESENTATION.toString()),
            Arguments.of("removeLegalRepresentative", REMOVE_LEGAL_REPRESENTATIVE.toString()),
            Arguments.of("requestFeeRemission", REQUEST_FEE_REMISSION.toString()),
            Arguments.of("manageFeeUpdate", MANAGE_FEE_UPDATE.toString()),
            Arguments.of("recordOutOfTimeDecision", RECORD_OUT_OF_TIME_DECISION.toString()),
            Arguments.of("editPaymentMethod", EDIT_PAYMENT_METHOD.toString()),
            Arguments.of("submitApplication", SUBMIT_APPLICATION.toString()),
            Arguments.of("uploadBailSummary", UPLOAD_BAIL_SUMMARY.toString()),
            Arguments.of("uploadSignedDecisionNotice", UPLOAD_SIGNED_DECISION_NOTICE.toString()),
            Arguments.of("uploadSignedDecisionNoticeConditionalGrant", UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT.toString()),
            Arguments.of("endApplication", END_APPLICATION.toString()),
            Arguments.of("uploadDocuments", UPLOAD_DOCUMENTS.toString()),
            Arguments.of("editBailDocuments", EDIT_BAIL_DOCUMENTS.toString()),
            Arguments.of("changeBailDirectionDueDate", CHANGE_BAIL_DIRECTION_DUE_DATE.toString()),
            Arguments.of("sendBailDirection", SEND_BAIL_DIRECTION.toString()),
            Arguments.of("makeNewApplication", MAKE_NEW_APPLICATION.toString()),
            Arguments.of("editBailApplicationAfterSubmit", EDIT_BAIL_APPLICATION_AFTER_SUBMIT.toString()),
            Arguments.of("stopLegalRepresenting", STOP_LEGAL_REPRESENTING.toString()),
            Arguments.of("nocRequestBail", NOC_REQUEST_BAIL.toString()),
            Arguments.of("endAppealAutomatically", END_APPEAL_AUTOMATICALLY.toString()),
            Arguments.of("updatePaymentStatus", UPDATE_PAYMENT_STATUS.toString()),
            Arguments.of("createCaseLink", Event.CREATE_CASE_LINK.toString()),
            Arguments.of("maintainCaseLinks", Event.MAINTAIN_CASE_LINKS.toString()),
            Arguments.of("createBailCaseLink", CREATE_BAIL_CASE_LINK.toString()),
            Arguments.of("maintainBailCaseLinks", Event.MAINTAIN_BAIL_CASE_LINKS.toString()),
            Arguments.of("adaSuitabilityReview", ADA_SUITABILITY_REVIEW.toString()),
            Arguments.of("transferOutOfAda", TRANSFER_OUT_OF_ADA.toString()),
            Arguments.of("markAppealAsAda", MARK_APPEAL_AS_ADA.toString()),
            Arguments.of("markAsReadyForUtTransfer", MARK_AS_READY_FOR_UT_TRANSFER.toString()),
            Arguments.of("updateDetentionLocation", UPDATE_DETENTION_LOCATION.toString()),
            Arguments.of("updateHearingAdjustments", UPDATE_HEARING_ADJUSTMENTS.toString()),
            Arguments.of("applyForCosts", APPLY_FOR_COSTS.toString()),
            Arguments.of("turnOnNotifications", TURN_ON_NOTIFICATIONS.toString()),
            Arguments.of("respondToCosts", RESPOND_TO_COSTS.toString()),
            Arguments.of("decideFtpaApplication", Event.DECIDE_FTPA_APPLICATION.toString()),
            Arguments.of("updateTribunalDecision", Event.UPDATE_TRIBUNAL_DECISION.toString()),
            Arguments.of("caseListing", CASE_LISTING.toString()),
            Arguments.of("markAppealAsRemitted", Event.MARK_APPEAL_AS_REMITTED.toString()),
            Arguments.of("addEvidenceForCosts", ADD_EVIDENCE_FOR_COSTS.toString()),
            Arguments.of("considerMakingCostsOrder", CONSIDER_MAKING_COSTS_ORDER.toString()),
            Arguments.of("decideCostsApplication", DECIDE_COSTS_APPLICATION.toString()),
            Arguments.of("recordTheDecision", RECORD_THE_DECISION.toString()),
            Arguments.of("changeTribunalCentre", CHANGE_TRIBUNAL_CENTRE.toString()),
            Arguments.of("sendPaymentReminderNotification", SEND_PAYMENT_REMINDER_NOTIFICATION.toString()),
            Arguments.of("progressMigratedCase", PROGRESS_MIGRATED_CASE.toString()),
            Arguments.of("recordRemissionReminder", Event.RECORD_REMISSION_REMINDER.toString()),
            Arguments.of("refundConfirmation", Event.REFUND_CONFIRMATION.toString()),
            Arguments.of("hearingCancelled", HEARING_CANCELLED.toString()),
            Arguments.of("revokeCitizenAccess", REVOKE_CITIZEN_ACCESS.toString()),
            Arguments.of("unknown", Event.UNKNOWN.toString()));
    }
}
