package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;

import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.FtpaNotificationPersonalisationUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class HomeOfficeFtpaApplicationDecisionRespondentPersonalisation implements EmailNotificationPersonalisation, FtpaNotificationPersonalisationUtil {

    private final PersonalisationProvider personalisationProvider;
    private final String upperTribunalNoticesEmailAddress;
    private final String upperTribunalPermissionApplicationsEmailAddress;
    private final String applicationGrantedApplicantHomeOfficeTemplateId;
    private final String applicationPartiallyGrantedApplicantHomeOfficeTemplateId;
    private final String applicationNotAdmittedApplicantHomeOfficeTemplateId;
    private final String applicationRefusedGrantedApplicantHomeOfficeTemplateId;
    private final String applicationReheardApplicantHomeHomeOfficeTemplateId;
    private final String applicationAllowedHomeOfficeTemplateId;
    private final String applicationDismissedHomeOfficeTemplateId;

    public HomeOfficeFtpaApplicationDecisionRespondentPersonalisation(
        @Value("${govnotify.template.applicationGranted.applicant.homeOffice.email}") String applicationGrantedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.applicant.homeOffice.email}") String applicationPartiallyGrantedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.applicant.homeOffice.email}") String applicationNotAdmittedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationRefused.applicant.homeOffice.email}") String applicationRefusedGrantedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationReheard.applicant.homeOffice.email}") String applicationReheardApplicantHomeHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationAllowed.homeOffice.email}") String applicationAllowedHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationDismissed.homeOffice.email}") String applicationDismissedHomeOfficeTemplateId,
        PersonalisationProvider personalisationProvider,
        @Value("${upperTribunalNoticesEmailAddress}") String upperTribunalNoticesEmailAddress,
        @Value("${upperTribunalPermissionApplicationsEmailAddress}") String upperTribunalPermissionApplicationsEmailAddress
    ) {
        this.applicationGrantedApplicantHomeOfficeTemplateId = applicationGrantedApplicantHomeOfficeTemplateId;
        this.applicationPartiallyGrantedApplicantHomeOfficeTemplateId = applicationPartiallyGrantedApplicantHomeOfficeTemplateId;
        this.applicationNotAdmittedApplicantHomeOfficeTemplateId = applicationNotAdmittedApplicantHomeOfficeTemplateId;
        this.applicationRefusedGrantedApplicantHomeOfficeTemplateId = applicationRefusedGrantedApplicantHomeOfficeTemplateId;
        this.applicationReheardApplicantHomeHomeOfficeTemplateId = applicationReheardApplicantHomeHomeOfficeTemplateId;
        this.applicationAllowedHomeOfficeTemplateId = applicationAllowedHomeOfficeTemplateId;
        this.applicationDismissedHomeOfficeTemplateId = applicationDismissedHomeOfficeTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.upperTribunalNoticesEmailAddress = upperTribunalNoticesEmailAddress;
        this.upperTribunalPermissionApplicationsEmailAddress = upperTribunalPermissionApplicationsEmailAddress;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        Optional<FtpaDecisionOutcomeType> ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);

        if (!ftpaDecisionOutcomeType.isPresent()) {
            ftpaDecisionOutcomeType = Optional.ofNullable(asylumCase
                .read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                .orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionOutcomeType is not present")));
        }

        if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return applicationGrantedApplicantHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString())) {
            return applicationPartiallyGrantedApplicantHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())) {
            return applicationRefusedGrantedApplicantHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && (ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD35.toString())
                                                           || ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD32.toString()))) {
            return applicationReheardApplicantHomeHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REMADE32.toString())) {
            FtpaDecisionOutcomeType ftpaDecisionRemade = asylumCase
                .read(FTPA_RESPONDENT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class)
                .orElseThrow(() -> new IllegalStateException("ftpaDecisionRemade is not present"));
            if (ftpaDecisionRemade.toString().equals(FtpaDecisionOutcomeType.FTPA_ALLOWED.toString())) {
                return applicationAllowedHomeOfficeTemplateId;
            }
            return applicationDismissedHomeOfficeTemplateId;
        } else {
            return applicationNotAdmittedApplicantHomeOfficeTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, State.class)
            .map(currentState -> {
                if (Arrays.asList(
                    State.FTPA_SUBMITTED,
                    State.FTPA_DECIDED).contains(currentState)
                ) {
                    String recipient = ftpaRespondentLjRjDecision(asylumCase)
                        .map(decision -> List.of(FTPA_GRANTED,FTPA_PARTIALLY_GRANTED).contains(decision)
                            ? upperTribunalPermissionApplicationsEmailAddress
                            : upperTribunalNoticesEmailAddress)
                        .orElseThrow(() -> new IllegalStateException("Appellant decision not present"));

                    return Collections.singleton(recipient);
                } else {
                    throw new IllegalStateException("homeOffice email Address cannot be found");
                }
            })
            .orElseThrow(() -> new IllegalStateException("homeOffice email Address cannot be found"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return this.personalisationProvider.getHomeOfficeHeaderPersonalisation(asylumCase);
    }

}
