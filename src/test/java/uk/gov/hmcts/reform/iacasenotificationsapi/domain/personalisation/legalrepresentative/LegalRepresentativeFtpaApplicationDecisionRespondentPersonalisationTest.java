package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_REMADE_RULE_32;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisationTest {

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "ariaListingReference";
    private final String homeOfficeRefNumber = "homeOfficeRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String otherPartyGrantedTemplateId = "otherPartyGrantedTemplateId";
    private final String otherPartyPartiallyGrantedTemplateId = "otherPartyPartiallyGrantedTemplateId";
    private final String otherPartyNotAdmittedTemplateId = "otherPartyNotAdmittedTemplateId";
    private final String otherPartyRefusedTemplateId = "otherPartyRefusedTemplateId";
    private final String otherPartyReheardTemplateId = "otherPartyReheardTemplateId";
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
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    private LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation
        legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation;

    @BeforeEach
    public void setup() {
        legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation =
            new LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation(
                otherPartyGrantedTemplateId,
                otherPartyPartiallyGrantedTemplateId,
                otherPartyNotAdmittedTemplateId,
                otherPartyRefusedTemplateId,
                otherPartyReheardTemplateId,
                allowedTemplateId,
                dismissedTemplateId,
                personalisationProvider
            );
    }

    @Test
    public void should_return_given_template_id_when_outcome_is_empty() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        IllegalStateException exception =
            assertThrows(IllegalStateException.class,
                () -> legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));
        assertEquals("ftpaRespondentDecisionOutcomeType is not present", exception.getMessage());
    }

    @Test
    public void should_return_given_template_id() {

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(granted));
        assertEquals(otherPartyGrantedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(partiallyGranted));
        assertEquals(otherPartyPartiallyGrantedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(notAdmitted));
        assertEquals(otherPartyNotAdmittedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(reheard));
        assertEquals(otherPartyReheardTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(allowed));
        assertEquals(allowedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(dismissed));
        assertEquals(dismissedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(refused));
        assertEquals(otherPartyRefusedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_RESPONDENT",
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_of_all_information_given(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation);
        when(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase))
            .thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation =
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("ariaListingReference", ariaListingReference)
            .containsEntry("homeOfficeRefNumber", homeOfficeRefNumber);
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeRefNumber", homeOfficeRefNumber)
            .build();
    }

}
