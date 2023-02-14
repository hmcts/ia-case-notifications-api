package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;

@Service
public class HomeOfficeFtpaApplicationDecisionRespondentPersonalisation implements EmailNotificationPersonalisation {

    private final DueDateService dueDateService;
    private final String upperTribunalNoticesEmailAddress;
    private final String applicationGrantedApplicantHomeOfficeTemplateId;
    private final String applicationPartiallyGrantedApplicantHomeOfficeTemplateId;
    private final String applicationNotAdmittedApplicantHomeOfficeTemplateId;
    private final String applicationRefusedGrantedApplicantHomeOfficeTemplateId;
    private final String applicationReheardApplicantHomeHomeOfficeTemplateId;
    private final String applicationAllowedHomeOfficeTemplateId;
    private final String applicationDismissedHomeOfficeTemplateId;
    private final int calendarDaysToWaitInCountry;
    private final int calendarDaysToWaitOutOfCountry;
    private final int workingDaysaysToWaitAda;

    public HomeOfficeFtpaApplicationDecisionRespondentPersonalisation(
        @Value("${govnotify.template.applicationGranted.applicant.homeOffice.email}") String applicationGrantedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.applicant.respondent.email}") String applicationPartiallyGrantedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.applicant.respondent.email}") String applicationNotAdmittedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationRefused.applicant.respondent.email}") String applicationRefusedGrantedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationReheard.applicant.homeOffice.email}") String applicationReheardApplicantHomeHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationAllowed.homeOffice.email}") String applicationAllowedHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationDismissed.homeOffice.email}") String applicationDismissedHomeOfficeTemplateId,
        @Value("${ftpaApplicationDecidedDaysToWait.inCountry}") int calendarDaysToWaitInCountry,
        @Value("${ftpaApplicationDecidedDaysToWait.outOfCountry}") int calendarDaysToWaitOutOfCountry,
        @Value("${ftpaApplicationDecidedDaysToWait.ada}") int workingDaysaysToWaitAda,
        DueDateService dueDateService,
        @Value("${upperTribunalNoticesEmailAddress}") String upperTribunalNoticesEmailAddress
    ) {
        this.applicationGrantedApplicantHomeOfficeTemplateId = applicationGrantedApplicantHomeOfficeTemplateId;
        this.applicationPartiallyGrantedApplicantHomeOfficeTemplateId = applicationPartiallyGrantedApplicantHomeOfficeTemplateId;
        this.applicationNotAdmittedApplicantHomeOfficeTemplateId = applicationNotAdmittedApplicantHomeOfficeTemplateId;
        this.applicationRefusedGrantedApplicantHomeOfficeTemplateId = applicationRefusedGrantedApplicantHomeOfficeTemplateId;
        this.applicationReheardApplicantHomeHomeOfficeTemplateId = applicationReheardApplicantHomeHomeOfficeTemplateId;
        this.applicationAllowedHomeOfficeTemplateId = applicationAllowedHomeOfficeTemplateId;
        this.applicationDismissedHomeOfficeTemplateId = applicationDismissedHomeOfficeTemplateId;
        this.calendarDaysToWaitInCountry = calendarDaysToWaitInCountry;
        this.calendarDaysToWaitOutOfCountry = calendarDaysToWaitOutOfCountry;
        this.workingDaysaysToWaitAda = workingDaysaysToWaitAda;
        this.dueDateService = dueDateService;
        this.upperTribunalNoticesEmailAddress = upperTribunalNoticesEmailAddress;
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
                    return Collections.singleton(upperTribunalNoticesEmailAddress);
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
        ImmutableMap.Builder<String, String> personalisationBuilder = ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("respondentReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""));

        boolean setDynamicDate = Arrays.asList(
            applicationPartiallyGrantedApplicantHomeOfficeTemplateId,
            applicationNotAdmittedApplicantHomeOfficeTemplateId,
            applicationRefusedGrantedApplicantHomeOfficeTemplateId).contains(getTemplateId(asylumCase));

        if (setDynamicDate) {
            boolean inCountryAppeal = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).map(value -> value.equals(YesOrNo.YES)).orElse(true);
            if (AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase)) {
                return personalisationBuilder.put("due date", dueDateService.calculateWorkingDaysDueDate(ZonedDateTime.now(), workingDaysaysToWaitAda)
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))).build();
            } else if (inCountryAppeal) {
                return personalisationBuilder.put("due date", dueDateService.calculateCalendarDaysDueDate(ZonedDateTime.now(), calendarDaysToWaitInCountry)
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))).build();
            } else {
                return personalisationBuilder.put("due date", dueDateService.calculateCalendarDaysDueDate(ZonedDateTime.now(), calendarDaysToWaitOutOfCountry)
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))).build();
            }
        }

        return personalisationBuilder.build();
    }

}
