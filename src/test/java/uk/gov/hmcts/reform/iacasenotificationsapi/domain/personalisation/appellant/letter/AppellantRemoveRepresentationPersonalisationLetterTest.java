package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantRemoveRepresentationPersonalisationLetterTest {
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PinInPostDetails pinInPostDetails;
    @Mock
    AddressUk appellantAddress;

    private final Long ccdCaseId = 1234555577779999L;
    private final String ccdCaseIdFormatted = "1234-5555-7777-9999";
    private String letterTemplateId = "someLetterTemplateId";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String addressLine1 = "Flat 230";
    private String addressLine2 = "No 100";
    private String addressLine3 = "New road street";
    private String postCode = "XX1 2YY";
    private String postTown = "postTown";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String securityCode = "securityCode";
    private String validDate = "2022-12-31";
    private String validDateFormatted = "31 Dec 2022";
    private String appellantDateOfBirth = "2000-01-01";
    private String appellantDateOfBirthFormatted = "1 Jan 2000";
    private String iaAipFrontendUrl = "iaAipFrontendUrl/";
    private String iaAipPathToSelfRepresentation = "iaAipPathToSelfRepresentation";
    private String linkToPiPStartPage = "iaAipFrontendUrl/iaAipPathToSelfRepresentation";

    private AppellantRemoveRepresentationPersonalisationLetter appellantRemoveRepresentationPersonalisationLetter;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(ccdCaseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(appellantAddress));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(pinInPostDetails.getAccessCode()).thenReturn(securityCode);
        when(pinInPostDetails.getExpiryDate()).thenReturn(validDate);
        when(appellantAddress.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(appellantAddress.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(appellantAddress.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(appellantAddress.getPostCode()).thenReturn(Optional.of(postCode));
        when(appellantAddress.getPostTown()).thenReturn(Optional.of(postTown));

        appellantRemoveRepresentationPersonalisationLetter = new AppellantRemoveRepresentationPersonalisationLetter(
            iaAipFrontendUrl,
            iaAipPathToSelfRepresentation,
            letterTemplateId,
            customerServicesProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, appellantRemoveRepresentationPersonalisationLetter.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_REMOVE_REPRESENTATION_APPELLANT_LETTER",
            appellantRemoveRepresentationPersonalisationLetter.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_address_in_correct_format() {
        assertTrue(appellantRemoveRepresentationPersonalisationLetter.getRecipientsList(asylumCase).contains("Flat230_No100_Newroadstreet_postTown_XX12YY"));
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantRemoveRepresentationPersonalisationLetter.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantAddress is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantRemoveRepresentationPersonalisationLetter.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            appellantRemoveRepresentationPersonalisationLetter.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appellantDateOfBirthFormatted, personalisation.get("appellantDateOfBirth"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(addressLine2, personalisation.get("address_line_2"));
        assertEquals(addressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(postCode, personalisation.get("address_line_5"));
        assertEquals(ccdCaseIdFormatted, personalisation.get("ccdCaseId"));
        assertEquals(linkToPiPStartPage, personalisation.get("linkToPiPStartPage"));
        assertEquals(securityCode, personalisation.get("securityCode"));
        assertEquals(validDateFormatted, personalisation.get("validDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(caseDetails.getId()).thenReturn(ccdCaseId);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());
        when(appellantAddress.getAddressLine2()).thenReturn(Optional.empty());
        when(appellantAddress.getAddressLine3()).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantRemoveRepresentationPersonalisationLetter.getPersonalisation(callback);

        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("appellantDateOfBirth"));
        assertEquals("", personalisation.get("securityCode"));
        assertEquals("", personalisation.get("validDate"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(postTown, personalisation.get("address_line_2"));
        assertEquals(postCode, personalisation.get("address_line_3"));
        assertEquals(ccdCaseIdFormatted, personalisation.get("ccdCaseId"));
        assertEquals(linkToPiPStartPage, personalisation.get("linkToPiPStartPage"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}