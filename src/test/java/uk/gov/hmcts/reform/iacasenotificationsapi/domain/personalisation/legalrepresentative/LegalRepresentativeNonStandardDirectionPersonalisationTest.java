package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeNonStandardDirectionPersonalisationTest {

    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String legalRepEmailAddress = "legalrep@example.com";
    private final String iaExUiFrontendUrl = "http://localhost";
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
    private LegalRepresentativeNonStandardDirectionPersonalisation
        legalRepresentativeNonStandardDirectionPersonalisation;

    @BeforeEach
    public void setUp() {

        String customerServicesTelephone = "555 555 555";
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        String customerServicesEmail = "customer.services@example.com";
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeNonStandardDirectionPersonalisation =
            new LegalRepresentativeNonStandardDirectionPersonalisation(
                beforeListingTemplateId,
                afterListingTemplateId,
                iaExUiFrontendUrl,
                personalisationProvider,
                customerServicesProvider
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(beforeListingTemplateId,
            legalRepresentativeNonStandardDirectionPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        assertEquals(afterListingTemplateId,
            legalRepresentativeNonStandardDirectionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_LEGAL_REP_NON_STANDARD_DIRECTION",
            legalRepresentativeNonStandardDirectionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> legalRepresentativeNonStandardDirectionPersonalisation
                .getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeNonStandardDirectionPersonalisation);

        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());

        Map<String, String> personalisation =
            legalRepresentativeNonStandardDirectionPersonalisation.getPersonalisation(callback);

        assertFalse(personalisation.isEmpty());
        assertThat(personalisation)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("subjectPrefix", isAda.equals(YesOrNo.YES) ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal")
            .containsAllEntriesOf(getPersonalisationMapWithGivenValues());
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        String directionDueDate = "2019-10-29";
        String directionExplanation = "someExplanation";
        String appellantFamilyNames = "someAppellantFamilyNames";
        String appellantGivenNames = "someAppellantGivenNames";
        String legalRepReferenceNumber = "someLegalRepReferenceNumber";
        String ariaListingReference = "someAriaListingReference";
        String appealReferenceNumber = "someReferenceNumber";
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("legalRepReferenceNumber", legalRepReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyNames)
            .put("explanation", directionExplanation)
            .put("dueDate", directionDueDate)
            .build();
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(legalRepresentativeNonStandardDirectionPersonalisation.isAppealListed(asylumCase));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        assertTrue(legalRepresentativeNonStandardDirectionPersonalisation.isAppealListed(asylumCase));
    }
}
