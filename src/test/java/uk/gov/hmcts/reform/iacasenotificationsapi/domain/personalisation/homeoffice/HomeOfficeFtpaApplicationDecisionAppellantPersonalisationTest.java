package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.List;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeFtpaApplicationDecisionAppellantPersonalisationTest {

    private final String upperTribunalNoticesEmailAddress = "homeoffice-granted@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String ariaListingReference = "ariaListingReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String otherPartyGrantedTemplateId = "otherPartyGrantedTemplateId";
    private final String otherPartyPartiallyGrantedTemplateId = "otherPartyPartiallyGrantedTemplateId";
    private final String otherPartyNotAdmittedTemplateId = "otherPartyNotAdmittedTemplateId";
    private final String otherPartyRefusedTemplateId = "otherPartyRefusedTemplateId";
    private final String otherPartyReheardTemplateId = "otherPartyReheardTemplateId";
    private final String allowedTemplateId = "allowedTemplateId";
    private final String dismissedTemplateId = "dismissedTemplateId";
    private final FtpaDecisionOutcomeType granted = FTPA_GRANTED;
    private final FtpaDecisionOutcomeType partiallyGranted = FTPA_PARTIALLY_GRANTED;
    private final FtpaDecisionOutcomeType notAdmitted = FTPA_NOT_ADMITTED;
    private final FtpaDecisionOutcomeType refused = FTPA_REFUSED;
    private final FtpaDecisionOutcomeType reheard = FTPA_REHEARD35;
    private final FtpaDecisionOutcomeType remade = FTPA_REMADE32;
    private final FtpaDecisionOutcomeType allowed = FTPA_ALLOWED;
    private final FtpaDecisionOutcomeType dismissed = FTPA_DISMISSED;
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    private HomeOfficeFtpaApplicationDecisionAppellantPersonalisation
        homeOfficeFtpaApplicationDecisionAppellantPersonalisation;

    @BeforeEach
    public void setup() {

        homeOfficeFtpaApplicationDecisionAppellantPersonalisation =
            new HomeOfficeFtpaApplicationDecisionAppellantPersonalisation(
                otherPartyGrantedTemplateId,
                otherPartyPartiallyGrantedTemplateId,
                otherPartyNotAdmittedTemplateId,
                otherPartyRefusedTemplateId,
                otherPartyReheardTemplateId,
                allowedTemplateId,
                dismissedTemplateId,
                personalisationProvider,
                upperTribunalNoticesEmailAddress
            );
    }

    @Test
    public void should_return_given_template_id_when_outcome_is_empty() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));
        assertEquals("ftpaAppellantDecisionOutcomeType is not present", exception.getMessage());
    }

    @Test
    public void should_return_given_template_id() {

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(granted));
        assertEquals(otherPartyGrantedTemplateId,
            homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(partiallyGranted));
        assertEquals(otherPartyPartiallyGrantedTemplateId,
            homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(notAdmitted));
        assertEquals(otherPartyNotAdmittedTemplateId,
            homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(reheard));
        assertEquals(otherPartyReheardTemplateId,
            homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(allowed));
        assertEquals(allowedTemplateId,
            homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(dismissed));
        assertEquals(dismissedTemplateId,
            homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(refused));
        assertEquals(otherPartyRefusedTemplateId,
            homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE_APPELLANT",
            homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getReferenceId(caseId));
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
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(decision));
        List.of(State.FTPA_SUBMITTED, State.FTPA_DECIDED).forEach(state -> {
            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, State.class))
                .thenReturn(Optional.of(state));

            assertTrue(homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getRecipientsList(asylumCase)
                .contains(upperTribunalNoticesEmailAddress));
        });
    }

    @Test
    void should_throw_exception_for_wrong_state() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, State.class))
            .thenReturn(Optional.of(State.DECIDED));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getRecipientsList(asylumCase));
        assertEquals("homeOffice email Address cannot be found", exception.getMessage());
    }

    @Test
    void should_throw_exception_for_missing_state() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, State.class))
            .thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getRecipientsList(asylumCase));
        assertEquals("homeOffice email Address cannot be found", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_of_all_information_given(YesOrNo isAda) {
        initializePrefixes(homeOfficeFtpaApplicationDecisionAppellantPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getHomeOfficeHeaderPersonalisation(asylumCase))
            .thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation =
            homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("homeOfficeRefNumber", homeOfficeRefNumber)
            .containsEntry("ariaListingReference", ariaListingReference);
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("homeOfficeRefNumber", homeOfficeRefNumber)
            .put("ariaListingReference", ariaListingReference)
            .build();
    }
}
