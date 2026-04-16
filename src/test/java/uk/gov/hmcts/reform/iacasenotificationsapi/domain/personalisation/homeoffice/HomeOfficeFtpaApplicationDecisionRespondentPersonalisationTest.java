package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeFtpaApplicationDecisionRespondentPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    DueDateService dueDateService;
    @Mock
    PersonalisationProvider personalisationProvider;

    private final String homeOfficeEmailAddress = "homeoffice-allowed@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String ariaListingReference = "ariaListingReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    private final String applicantGrantedTemplateId = "applicantGrantedTemplateId";
    private final String applicantPartiallyGrantedTemplateId = "applicantPartiallyGrantedTemplateId";
    private final String applicantNotAdmittedTemplateId = "applicantNotAdmittedTemplateId";
    private final String applicantRefusedTemplateId = "applicantRefusedTemplateId";
    private final String applicantReheardTemplateId = "otherPartyReheardTemplateId";
    private final String allowedTemplateId = "allowedTemplateId";
    private final String dismissedTemplateId = "dismissedTemplateId";

    private final FtpaDecisionOutcomeType granted = FtpaDecisionOutcomeType.FTPA_GRANTED;
    private final FtpaDecisionOutcomeType partiallyGranted = FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
    private final FtpaDecisionOutcomeType notAdmitted = FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED;
    private final FtpaDecisionOutcomeType refused = FtpaDecisionOutcomeType.FTPA_REFUSED;
    private final FtpaDecisionOutcomeType reheard = FtpaDecisionOutcomeType.FTPA_REHEARD35;
    private final FtpaDecisionOutcomeType remade = FtpaDecisionOutcomeType.FTPA_REMADE32;
    private final FtpaDecisionOutcomeType allowed = FtpaDecisionOutcomeType.FTPA_ALLOWED;
    private final FtpaDecisionOutcomeType dismissed = FtpaDecisionOutcomeType.FTPA_DISMISSED;

    private HomeOfficeFtpaApplicationDecisionRespondentPersonalisation
        homeOfficeFtpaApplicationDecisionRespondentPersonalisation;

    @BeforeEach
    public void setup() {

        int workingDaysaysToWaitAda = 5;
        int calendarDaysToWaitOutOfCountry = 28;
        int calendarDaysToWaitInCountry = 14;
        homeOfficeFtpaApplicationDecisionRespondentPersonalisation =
            new HomeOfficeFtpaApplicationDecisionRespondentPersonalisation(
                applicantGrantedTemplateId,
                applicantPartiallyGrantedTemplateId,
                applicantNotAdmittedTemplateId,
                applicantRefusedTemplateId,
                applicantReheardTemplateId,
                allowedTemplateId,
                dismissedTemplateId,
                calendarDaysToWaitInCountry,
                calendarDaysToWaitOutOfCountry,
                workingDaysaysToWaitAda,
                dueDateService,
                personalisationProvider,
                homeOfficeEmailAddress
            );
    }

    @Test
    public void should_return_given_template_id_when_outcome_is_empty() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        IllegalStateException exception = 
assertThrows(IllegalStateException.class, () -> homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase))
            ;
assertEquals("ftpaRespondentDecisionOutcomeType is not present", exception.getMessage());
    }

    @Test
    public void should_return_given_template_id() {

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(granted));
        assertEquals(applicantGrantedTemplateId,
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(partiallyGranted));
        assertEquals(applicantPartiallyGrantedTemplateId,
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(notAdmitted));
        assertEquals(applicantNotAdmittedTemplateId,
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(reheard));
        assertEquals(applicantReheardTemplateId,
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(allowed));
        assertEquals(allowedTemplateId,
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(dismissed));
        assertEquals(dismissedTemplateId,
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(refused));
        assertEquals(applicantRefusedTemplateId,
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT",
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = FtpaDecisionOutcomeType.class, names = {
        "FTPA_GRANTED",
        "FTPA_PARTIALLY_GRANTED",
        "FTPA_REFUSED",
        "FTPA_NOT_ADMITTED",
        "FTPA_REHEARD35",
        "FTPA_REHEARD32",
        "FTPA_REMADE32",
        "FTPA_ALLOWED",
        "FTPA_DISMISSED"
    })
    void should_return_given_email_address_for_correct_states(FtpaDecisionOutcomeType decision) {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(decision));

        Arrays.asList(State.FTPA_SUBMITTED,State.FTPA_DECIDED)
            .forEach(state -> {
                when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, State.class))
                    .thenReturn(Optional.of(state));

                assertTrue(homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getRecipientsList(asylumCase)
                    .contains(homeOfficeEmailAddress));
            });
    }

    @Test
    void should_throw_exception_for_wrong_state() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, State.class))
            .thenReturn(Optional.of(State.DECIDED));

        IllegalStateException exception = 
assertThrows(IllegalStateException.class, () -> homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getRecipientsList(asylumCase))
            ;
assertEquals("homeOffice email Address cannot be found", exception.getMessage());
    }

    @Test
    void should_throw_exception_for_missing_state() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, State.class))
            .thenReturn(Optional.empty());

        IllegalStateException exception = 
assertThrows(IllegalStateException.class, () -> homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getRecipientsList(asylumCase))
            ;
assertEquals("homeOffice email Address cannot be found", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_of_all_information_given_decision_partially_granted_ada() {
        initializePrefixes(homeOfficeFtpaApplicationDecisionRespondentPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(personalisationProvider.getRespondentHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationforHomeOffice());
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(partiallyGranted));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(dueDateService.calculateWorkingDaysDueDate(any(ZonedDateTime.class), any(Integer.class))).thenReturn(ZonedDateTime.now());
        Map<String, String> personalisation =
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("respondentReferenceNumber", homeOfficeRefNumber)
            .containsEntry("subjectPrefix", "Accelerated detained appeal");

        verify(dueDateService, times(1)).calculateWorkingDaysDueDate(any(ZonedDateTime.class), any(Integer.class));
    }

    @Test
    public void should_return_personalisation_of_all_information_given_decision_refused_in_country() {
        initializePrefixes(homeOfficeFtpaApplicationDecisionRespondentPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(personalisationProvider.getRespondentHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationforHomeOffice());
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(refused));
        when(dueDateService.calculateCalendarDaysDueDate(any(ZonedDateTime.class), any(Integer.class))).thenReturn(ZonedDateTime.now());
        Map<String, String> personalisation =
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("respondentReferenceNumber", homeOfficeRefNumber)
            .containsEntry("subjectPrefix", "Immigration and Asylum appeal");

        verify(dueDateService, times(1)).calculateCalendarDaysDueDate(any(ZonedDateTime.class), any(Integer.class));
    }

    @Test
    public void should_return_personalisation_of_all_information_given_decision_not_admitted_out_of_country() {
        initializePrefixes(homeOfficeFtpaApplicationDecisionRespondentPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(personalisationProvider.getRespondentHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationforHomeOffice());
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(notAdmitted));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(dueDateService.calculateCalendarDaysDueDate(any(ZonedDateTime.class), any(Integer.class))).thenReturn(ZonedDateTime.now());
        Map<String, String> personalisation =
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("respondentReferenceNumber", homeOfficeRefNumber)
            .containsEntry("subjectPrefix", "Immigration and Asylum appeal");

        verify(dueDateService, times(1)).calculateCalendarDaysDueDate(any(ZonedDateTime.class), any(Integer.class));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_of_all_information_given_others(YesOrNo isAda) {
        initializePrefixes(homeOfficeFtpaApplicationDecisionRespondentPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getRespondentHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationforHomeOffice());
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(granted));
        Map<String, String> personalisation =
            homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber);
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

        verify(dueDateService, times(0)).calculateWorkingDaysDueDate(any(ZonedDateTime.class), any(Integer.class));
        verify(dueDateService, times(0)).calculateCalendarDaysDueDate(any(ZonedDateTime.class), any(Integer.class));
    }

    private Map<String, String> getPersonalisationforHomeOffice() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("respondentReferenceNumber", homeOfficeRefNumber)
            .put("ariaListingReference", ariaListingReference)
            .build();
    }
}
