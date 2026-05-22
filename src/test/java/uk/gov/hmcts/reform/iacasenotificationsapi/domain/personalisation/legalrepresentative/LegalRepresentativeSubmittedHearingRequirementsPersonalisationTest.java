package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeSubmittedHearingRequirementsPersonalisationTest {

    private final String templateId = "someTemplateId";
    private final String adaTemplateId = "someAdaTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String legalRepEmailAddress = "legalrep@something.com";
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    private LegalRepresentativeSubmittedHearingRequirementsPersonalisation
        legalRepresentativeSubmittedHearingRequirementsPersonalisation;

    @BeforeEach
    public void setUp() {

        legalRepresentativeSubmittedHearingRequirementsPersonalisation =
            new LegalRepresentativeSubmittedHearingRequirementsPersonalisation(
                templateId,
                adaTemplateId,
                iaExUiFrontendUrl,
                personalisationProvider,
                customerServicesProvider
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeSubmittedHearingRequirementsPersonalisation.getTemplateId(asylumCase));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertEquals(adaTemplateId, legalRepresentativeSubmittedHearingRequirementsPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_LEGAL_REP_OF_SUBMITTED_HEARING_REQUIREMENTS",
            legalRepresentativeSubmittedHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeSubmittedHearingRequirementsPersonalisation);
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisation());
        Map<String, String> personalisation =
            legalRepresentativeSubmittedHearingRequirementsPersonalisation.getPersonalisation(callback);
        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal")
            .containsAllEntriesOf(getPersonalisation());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> legalRepresentativeSubmittedHearingRequirementsPersonalisation
                .getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    private Map<String, String> getPersonalisation() {
        String customerServicesEmail = "cust.services@example.com";
        String customerServicesTelephone = "555 555 555";
        String appellantFamilyName = "someAppellantFamilyName";
        String appellantGivenNames = "someAppellantGivenNames";
        String legalRepReferenceNumber = "legalRepReferenceNumber";
        String appealReferenceNumber = "someReferenceNumber";
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("legalRepReferenceNumber", legalRepReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("customerServicesTelephone", customerServicesTelephone)
            .put("customerServicesEmail", customerServicesEmail)
            .build();
    }
}
