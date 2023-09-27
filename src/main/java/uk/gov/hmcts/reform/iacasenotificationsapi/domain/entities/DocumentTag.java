package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentTag {

    CASE_ARGUMENT("caseArgument"),
    RESPONDENT_EVIDENCE("respondentEvidence"),
    APPEAL_RESPONSE("appealResponse"),
    APPEAL_SUBMISSION("appealSubmission"),
    ADDITIONAL_EVIDENCE("additionalEvidence"),
    HEARING_NOTICE("hearingNotice"),
    CASE_SUMMARY("caseSummary"),
    HEARING_BUNDLE("hearingBundle"),
    ADDENDUM_EVIDENCE("addendumEvidence"),
    DECISION_AND_REASONS_DRAFT("decisionAndReasons"),
    DECISION_AND_REASONS_COVER_LETTER("decisionAndReasonsCoverLetter"),
    FINAL_DECISION_AND_REASONS_PDF("finalDecisionAndReasonsPdf"),
    APPEAL_SKELETON_BUNDLE("submitCaseBundle"),
    HO_DECISION_LETTER("homeOfficeDecisionLetter"),
    END_APPEAL("endAppeal"),
    RECORD_OUT_OF_TIME_DECISION_DOCUMENT("recordOutOfTimeDecisionDocument"),
    BAIL_EVIDENCE("uploadTheBailEvidenceDocs"),
    APPLICATION_SUBMISSION("applicationSubmission"),
    BAIL_SUMMARY("uploadBailSummary"),
    SIGNED_DECISION_NOTICE("signedDecisionNotice"),
    BAIL_DECISION_UNSIGNED("bailDecisionUnsigned"),
    UPLOAD_DOCUMENT("uploadDocument"),
    BAIL_SUBMISSION("bailSubmission"),
    B1_DOCUMENT("b1Document"),
    ADA_SUITABILITY("adaSuitability"),
    INTERNAL_ADA_SUITABILITY("internalAdaSuitability"),
    APPEAL_FORM("appealForm"),
    FTPA_DECISION_AND_REASONS("ftpaDecisionAndReasons"),
    FTPA_APPELLANT("ftpaAppellant"),
    FTPA_RESPONDENT("ftpaRespondent"),
    REQUEST_CASE_BUILDING("requestCaseBuilding"),
    REQUEST_RESPONDENT_REVIEW("requestRespondentReview"),
    UPLOAD_THE_APPEAL_RESPONSE("uploadTheAppealResponse"),
    HEARING_BUNDLE_READY_LETTER("hearingBundleReadyLetter"),
    INTERNAL_DET_DECISION_AND_REASONS_LETTER("internalDetDecisionAndReasonsLetter"),
    INTERNAL_APPEAL_SUBMISSION("internalAppealSubmission"),
    INTERNAL_REQUEST_RESPONDENT_EVIDENCE_LETTER("internalRequestRespondentEvidenceLetter"),
    INTERNAL_END_APPEAL_AUTOMATICALLY("internalEndAppealAutomatically"),
    INTERNAL_APPEAL_FEE_DUE_LETTER("internalAppealFeeDueLetter"),
    INTERNAL_DET_MARK_AS_PAID_LETTER("internalDetMarkAsPaidLetter"),
    INTERNAL_LIST_CASE_LETTER("internalListCaseLetter"),
    INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW("internalDetainedRequestHomeOfficeResponseReview"),
    INTERNAL_REQUEST_HEARING_REQUIREMENTS_LETTER("internalRequestHearingRequirementsLetter"),
    INTERNAL_DECIDE_AN_APPELLANT_APPLICATION_LETTER("internalDecideAnAppellantApplicationLetter"),
    INTERNAL_DECIDE_HOME_OFFICE_APPLICATION_LETTER("internalDecideHomeOfficeApplicationLetter"),
    INTERNAL_DET_MARK_AS_ADA_LETTER("internalDetMarkAsAdaLetter"),
    INTERNAL_DETAINED_EDIT_CASE_LISTING_LETTER("internalDetainedEditCaseListingLetter"),
    INTERNAL_APPLY_FOR_FTPA_RESPONDENT("internalApplyForFtpaRespondent"),
    INTERNAL_FTPA_SUBMITTED_APPELLANT_LETTER("internalFtpaSubmittedAppellantLetter"),
    INTERNAL_DETAINED_TRANSFER_OUT_OF_ADA_LETTER("internalDetainedTransferOutOfAdaLetter"),
    INTERNAL_HO_FTPA_DECIDED_LETTER("internalHoFtpaDecidedLetter"),
    INTERNAL_APPELLANT_FTPA_DECIDED_LETTER("internalAppellantFtpaDecidedLetter"),
    INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER("internalNonStandardDirectionToAppellantLetter"),
    INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT_LETTER("internalNonStandardDirectionRespondentLetter"),
    INTERNAL_HEARING_REQUIREMENTS_UPDATED_LETTER("internalHearingRequirementsUpdatedLetter"),
    MAINTAIN_CASE_UNLINK_APPEAL_LETTER("maintainCaseUnlinkAppealLetter"),
    INTERNAL_CHANGE_HEARING_CENTRE_LETTER("internalChangeHearingCentreLetter"),
    MAINTAIN_CASE_LINK_APPEAL_LETTER("maintainCaseLinkAppealLetter"),
    INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER("internalUploadAdditionalEvidenceLetter"),
    AMEND_HOME_OFFICE_APPEAL_RESPONSE("amendHomeOfficeAppealResponse"),
    EDIT_APPEAL_LETTER("editAppealLetter"),

    @JsonEnumDefaultValue
    NONE("");

    @JsonValue
    private final String id;

    DocumentTag(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
