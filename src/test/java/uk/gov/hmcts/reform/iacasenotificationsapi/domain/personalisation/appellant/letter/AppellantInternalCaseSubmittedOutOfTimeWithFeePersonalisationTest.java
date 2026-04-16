package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantInternalCaseSubmittedOutOfTimeWithFeePersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AddressUk address;

    private final Long ccdCaseId = 12345L;
    private final String letterTemplateId = "someLetterTemplateId";
    private final String appealReferenceNumber = "someAppealRefNumber";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String addressLine1 = "50";
    private final String addressLine2 = "Building name";
    private final String addressLine3 = "Street name";
    private final String postCode = "XX1 2YY";
    private final NationalityFieldValue oocCountry = mock(NationalityFieldValue.class);
    private final String postTown = "Town name";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "example@example.com";
    private final SystemDateProvider systemDateProvider = new SystemDateProvider();
    private final String amountLeftToPayInGbp = "140.00";
    private final String feeAmountGbp = "14000";
    private AppellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(ccdCaseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));

        int daysAfterSubmitAppeal = 14;
        appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation = new AppellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation(
            letterTemplateId,
            daysAfterSubmitAppeal,
            customerServicesProvider,
            systemDateProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_INTERNAL_SUBMIT_APPEAL_WITH_FEE_OUT_OF_TIME_APPELLANT_LETTER",
            appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_address_in_correct_format_appellant_in_country() {
        appellantInCountryDataSetup();
        assertTrue(appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));
    }

    @Test
    void should_return_address_in_correct_format_appellant_out_of_country() {
        appellantOutOfCountryDataSetup();
        assertTrue(appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_Spain"));
    }

    @Test
    void should_return_address_in_correct_format_legalRep_in_country() {
        legalRepInCountryDataSetup();
        assertTrue(appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));
    }

    @Test
    void should_return_address_in_correct_format_legalRep_out_of_country() {
        legalRepOutOfCountryDataSetup();
        assertTrue(appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_Spain"));
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getRecipientsList(asylumCase));
        assertEquals("appellantAddress is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_legalRep_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getRecipientsList(asylumCase));
        assertEquals("legalRepAddressUK is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given_appellant_in_country() {
        appellantInCountryDataSetup();
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmountGbp));
        Map<String, String> personalisation =
            appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("fourteenDaysAfterSubmitDate", systemDateProvider.dueDate(14))
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", addressLine1)
            .containsEntry("address_line_2", addressLine2)
            .containsEntry("address_line_3", addressLine3)
            .containsEntry("address_line_4", postTown)
            .containsEntry("address_line_5", postCode);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
    }

    @Test
    void should_return_personalisation_when_all_information_given_appellant_out_of_country() {
        appellantOutOfCountryDataSetup();
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmountGbp));
        Map<String, String> personalisation =
            appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("fourteenDaysAfterSubmitDate", systemDateProvider.dueDate(14))
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", addressLine1)
            .containsEntry("address_line_2", addressLine2)
            .containsEntry("address_line_3", addressLine3)
            .containsEntry("address_line_4", postTown)
            .containsEntry("address_line_5", Nationality.ES.toString());
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
    }

    @Test
    void should_return_personalisation_when_all_information_given_legalRep_in_country() {
        legalRepInCountryDataSetup();
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmountGbp));
        Map<String, String> personalisation =
            appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("fourteenDaysAfterSubmitDate", systemDateProvider.dueDate(14))
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", addressLine1)
            .containsEntry("address_line_2", addressLine2)
            .containsEntry("address_line_3", addressLine3)
            .containsEntry("address_line_4", postTown)
            .containsEntry("address_line_5", postCode);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
    }

    @Test
    void should_return_personalisation_when_all_information_given_legalRep_out_of_country() {
        legalRepOutOfCountryDataSetup();
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmountGbp));
        Map<String, String> personalisation =
            appellantInternalCaseSubmittedOutOfTimeWithFeePersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("fourteenDaysAfterSubmitDate", systemDateProvider.dueDate(14))
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", addressLine1)
            .containsEntry("address_line_2", addressLine2)
            .containsEntry("address_line_3", addressLine3)
            .containsEntry("address_line_4", postTown)
            .containsEntry("address_line_5", Nationality.ES.toString());
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
    }

    private void appellantOutOfCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(addressLine1));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(addressLine2));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(addressLine3));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_4_ADMIN_J, String.class)).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.COUNTRY_GOV_UK_OOC_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocCountry));
        when(oocCountry.getCode()).thenReturn(Nationality.ES.name());
    }

    private void appellantInCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
    }

    private void legalRepOutOfCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_1, String.class)).thenReturn(Optional.of(addressLine1));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_2, String.class)).thenReturn(Optional.of(addressLine2));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_3, String.class)).thenReturn(Optional.of(addressLine3));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_4, String.class)).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.OOC_LR_COUNTRY_GOV_UK_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocCountry));
        when(oocCountry.getCode()).thenReturn(Nationality.ES.name());
    }

    private void legalRepInCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.of(address));
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
    }
}
