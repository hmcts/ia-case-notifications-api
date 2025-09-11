package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;

public enum UserRole {
    // caseworker-ia-caseofficer
    CASE_OFFICER("caseworker-ia-caseofficer"),
    TRIBUNAL_CASEWORKER("tribunal-caseworker"),
    CHALLENGED_ACCESS_LEGAL_OPERATIONS("challenged-access-legal-operations"),
    SENIOR_TRIBUNAL_CASEWORKER("senior-tribunal-caseworker"),
    // caseworker-ia-admofficer
    ADMIN_OFFICER("caseworker-ia-admofficer"),
    HEARING_CENTRE_ADMIN("hearing-centre-admin"),
    CTSC("ctsc"),
    CTSC_TEAM_LEADER("ctsc-team-leader"),
    NATIONAL_BUSINESS_CENTRE("national-business-centre"),
    CHALLENGED_ACCESS_CTSC("challenged-access-ctsc"),
    CHALLENGED_ACCESS_ADMIN("challenged-access-admin"),
    // caseworker-ia-iacjudge
    IDAM_JUDGE("caseworker-ia-iacjudge"),
    JUDICIARY("caseworker-ia-judiciary"),
    JUDGE("judge"),
    SENIOR_JUDGE("senior-judge"),
    LEADERSHIP_JUDGE("leadership-judge"),
    FEE_PAID_JUDGE("fee-paid-judge"),
    LEAD_JUDGE("lead-judge"),
    HEARING_JUDGE("hearing-judge"),
    FTPA_JUDGE("ftpa-judge"),
    HEARING_PANEL_JUDGE("hearing-panel-judge"),
    CHALLENGED_ACCESS_JUDICIARY("challenged-access-judiciary"),

    LEGAL_REPRESENTATIVE("caseworker-ia-legalrep-solicitor"),
    SYSTEM("caseworker-ia-system"),
    HOME_OFFICE_APC("caseworker-ia-homeofficeapc"),
    HOME_OFFICE_LART("caseworker-ia-homeofficelart"),
    HOME_OFFICE_POU("caseworker-ia-homeofficepou"),
    HOME_OFFICE_GENERIC("caseworker-ia-respondentofficer"),
    CITIZEN("citizen"),
    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    UserRole(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    public static List<UserRole> getAdminOfficerRoles() {
        return List.of(
            ADMIN_OFFICER,
            HEARING_CENTRE_ADMIN,
            CTSC,
            CTSC_TEAM_LEADER,
            NATIONAL_BUSINESS_CENTRE,
            CHALLENGED_ACCESS_CTSC,
            CHALLENGED_ACCESS_ADMIN
        );
    }

    public static UserRole getById(String id) {
        for (UserRole role : UserRole.values()) {
            if (role.id.equals(id)) {
                return role;
            }
        }
        return UNKNOWN;
    }
}
