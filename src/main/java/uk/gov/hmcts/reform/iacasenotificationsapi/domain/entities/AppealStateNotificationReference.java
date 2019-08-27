package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;


public enum AppealStateNotificationReference {

    END_APPEAL_HOME_OFFICE("_END_APPEAL_HOME_OFFICE"),
    END_APPEAL_LEGAL_REPRESENTATIVE("_END_APPEAL_LEGAL_REPRESENTATIVE"),
    EDIT_CASE_LISTING_HOME_OFFICE("_EDIT_CASE_LISTING_HOME_OFFICE"),
    EDIT_CASE_LISTING_LEGAL_REPRESENTATIVE("_EDIT_CASE_LISTING_LEGAL_REPRESENTATIVE");

    private String state;

    AppealStateNotificationReference(String state) {
        this.state = state;
    }

    public String state() {
        return state;
    }

    @Override
    public String toString() {
        return state();
    }

}
