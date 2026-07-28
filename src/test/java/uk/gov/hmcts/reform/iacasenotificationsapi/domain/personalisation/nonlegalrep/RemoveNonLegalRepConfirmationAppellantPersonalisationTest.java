package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.nonlegalrep;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NLR_DETAILS;

import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RemoveNonLegalRepConfirmationAppellantPersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private final Long caseId = 12345L;
    private final String templateId = "removeNonLegalRepConfirmationAppellantTemplateId";
    private final NonLegalRepDetails nlrDetails = NonLegalRepDetails.builder()
        .emailAddress("nlr@example.com")
        .givenNames("someGivenNames")
        .familyName("someFamilyName")
        .idamId("someIdamId")
        .build();
    private final String appealReferenceNumber = "hmctsReference";
    private final String homeOfficeReference = "homeOfficeReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String appellantEmail = "appellant@example.com";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";

    private RemoveNonLegalRepConfirmationAppellantPersonalisation removeNonLegalRepConfirmationAppellantPersonalisation;

    @BeforeEach
    void setUp() {
        removeNonLegalRepConfirmationAppellantPersonalisation = new RemoveNonLegalRepConfirmationAppellantPersonalisation(
            templateId,
            recipientsFinder,
            customerServicesProvider
        );
    }

    @Test
    void should_return_appellant_email_from_recipients_finder() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(appellantEmail));
        assertEquals(Collections.singleton(appellantEmail),
            removeNonLegalRepConfirmationAppellantPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {
        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> removeNonLegalRepConfirmationAppellantPersonalisation.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId,
            removeNonLegalRepConfirmationAppellantPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REMOVE_NON_LEGAL_REP_CONFIRMATION_APPELLANT_EMAIL",
            removeNonLegalRepConfirmationAppellantPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReference));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        Map<String, String> customerServicesPersonalisation = Map.of(
            "customerServicesTelephone", customerServicesTelephone,
            "customerServicesEmail", customerServicesEmail
        );
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(nlrDetails));
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(customerServicesPersonalisation);

        Map<String, String> personalisation =
            removeNonLegalRepConfirmationAppellantPersonalisation.getPersonalisation(asylumCase);

        assertFalse(personalisation.isEmpty());
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReference, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(customerServicesTelephone, personalisation.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, personalisation.get("customerServicesEmail"));
        assertEquals(nlrDetails.getGivenNames(), personalisation.get("nlrGivenNames"));
        assertEquals(nlrDetails.getFamilyName(), personalisation.get("nlrFamilyName"));
    }

    @Test
    void should_return_personalisation_when_nlr_names_empty() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReference));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        Map<String, String> customerServicesPersonalisation = Map.of(
            "customerServicesTelephone", customerServicesTelephone,
            "customerServicesEmail", customerServicesEmail
        );
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.empty());
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(customerServicesPersonalisation);

        Map<String, String> personalisation =
            removeNonLegalRepConfirmationAppellantPersonalisation.getPersonalisation(asylumCase);

        assertFalse(personalisation.isEmpty());
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReference, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(customerServicesTelephone, personalisation.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, personalisation.get("customerServicesEmail"));
        assertEquals("Sir /", personalisation.get("nlrGivenNames"));
        assertEquals("Madam", personalisation.get("nlrFamilyName"));
    }

    @Test
    void should_throw_exception_when_asylum_case_is_null() {
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> removeNonLegalRepConfirmationAppellantPersonalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
