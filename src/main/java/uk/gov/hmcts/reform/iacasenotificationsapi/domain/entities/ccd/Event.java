package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Event {

    START_APPEAL("startAppeal"),
    EDIT_APPEAL("editAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    SEND_DIRECTION("sendDirection"),
    REQUEST_RESPONDENT_EVIDENCE("requestRespondentEvidence"),
    UPLOAD_RESPONDENT_EVIDENCE("uploadRespondentEvidence"),
    UPLOAD_HOME_OFFICE_BUNDLE("uploadHomeOfficeBundle"),
    BUILD_CASE("buildCase"),
    SUBMIT_CASE("submitCase"),
    REQUEST_CASE_EDIT("requestCaseEdit"),
    REQUEST_RESPONDENT_REVIEW("requestRespondentReview"),
    ADD_APPEAL_RESPONSE("addAppealResponse"),
    REQUEST_HEARING_REQUIREMENTS("requestHearingRequirements"),
    REQUEST_HEARING_REQUIREMENTS_FEATURE("requestHearingRequirementsFeature"),
    DRAFT_HEARING_REQUIREMENTS("draftHearingRequirements"),
    CHANGE_DIRECTION_DUE_DATE("changeDirectionDueDate"),
    UPLOAD_ADDITIONAL_EVIDENCE("uploadAdditionalEvidence"),
    LIST_CASE("listCase"),
    CREATE_CASE_SUMMARY("createCaseSummary"),
    REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE("revertStateToAwaitingRespondentEvidence"),
    GENERATE_HEARING_BUNDLE("generateHearingBundle"),
    EDIT_CASE_LISTING("editCaseListing"),
    END_APPEAL("endAppeal"),
    RECORD_APPLICATION("recordApplication"),
    REQUEST_CASE_BUILDING("requestCaseBuilding"),
    UPLOAD_HOME_OFFICE_APPEAL_RESPONSE("uploadHomeOfficeAppealResponse"),
    REQUEST_RESPONSE_REVIEW("requestResponseReview"),
    SEND_DECISION_AND_REASONS("sendDecisionAndReasons"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
