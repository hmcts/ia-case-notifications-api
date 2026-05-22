package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeReheardUnderRule35PersonalisationTest {
    private final Long caseId = 12345L;
    private final String legalRepReheardUnder35RuleEmailTemplateId = "legalRepReheardUnder35RuleEmailTemplateId";
    private final String exUiFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String legalRepReferenceNumber = "legalRepReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    private LegalRepresentativeReheardUnderRule35Personalisation legalRepresentativeReheardUnderRule35Personalisation;

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        legalRepresentativeReheardUnderRule35Personalisation = new LegalRepresentativeReheardUnderRule35Personalisation(
            legalRepReheardUnder35RuleEmailTemplateId,
            exUiFrontendUrl,
            customerServicesProvider
        );
    }

    @Test
    void should_return_correct_template_id() {
        assertEquals(legalRepReheardUnder35RuleEmailTemplateId, legalRepresentativeReheardUnderRule35Personalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_LEGAL_REP_REHEARD_UNDER_RULE_35_EMAIL",
            legalRepresentativeReheardUnderRule35Personalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        String legalRepEmail = "example@example.com";
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmail));

        assertTrue(
            legalRepresentativeReheardUnderRule35Personalisation.getRecipientsList(asylumCase).contains(legalRepEmail));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> legalRepresentativeReheardUnderRule35Personalisation.getRecipientsList(asylumCase));
        assertEquals("legalRepresentativeEmailAddress is not present", exception.getMessage());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> legalRepresentativeReheardUnderRule35Personalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation = legalRepresentativeReheardUnderRule35Personalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("legalRepReferenceNumber", legalRepReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToService", exUiFrontendUrl);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
