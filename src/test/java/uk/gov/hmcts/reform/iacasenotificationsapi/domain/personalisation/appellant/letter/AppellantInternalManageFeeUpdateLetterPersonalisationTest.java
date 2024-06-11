package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantInternalManageFeeUpdateLetterPersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AddressUk appellantAddress;

    private Long ccdCaseId = 12345L;
    private String letterTemplateId = "someLetterTemplateId";
    private String appealReferenceNumber = "someAppealRefNumber";
    private String paidAmount = "14000";
    private String newFeeAmount = "10000";
    private String referenceNumber = "1111222233334444";
    private String feeUpdateReason = "some test reason";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String addressLine1 = "50";
    private String addressLine2 = "Building name";
    private String addressLine3 = "Street name";
    private String postCode = "XX1 2YY";
    private String postTown = "Town name";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "example@example.com";
    private final SystemDateProvider systemDateProvider = new SystemDateProvider();
    private int afterManageFeeEvent = 14;
    private String amountLeftToPay = "4000";
    private String amountLeftToPayInGbp = "40.00";
    private AppellantInternalManageFeeUpdateLetterPersonalisation appellantInternalManageFeeUpdateLetterPersonalisation;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(ccdCaseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(appellantAddress));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(AsylumCaseDefinition.PAID_AMOUNT, String.class)).thenReturn(Optional.of(paidAmount));
        when(asylumCase.read(AsylumCaseDefinition.NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of(newFeeAmount));
        when(asylumCase.read(AsylumCaseDefinition.FEE_UPDATE_REASONS, String.class)).thenReturn(Optional.of(feeUpdateReason));
        when(asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(referenceNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(appellantAddress.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(appellantAddress.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(appellantAddress.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(appellantAddress.getPostCode()).thenReturn(Optional.of(postCode));
        when(appellantAddress.getPostTown()).thenReturn(Optional.of(postTown));

        appellantInternalManageFeeUpdateLetterPersonalisation = new AppellantInternalManageFeeUpdateLetterPersonalisation(
            letterTemplateId,
            afterManageFeeEvent,
            customerServicesProvider,
            systemDateProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, appellantInternalManageFeeUpdateLetterPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_INTERNAL_MANAGE_FEE_UPDATE_APPELLANT_LETTER",
            appellantInternalManageFeeUpdateLetterPersonalisation.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_address_in_correct_format() {
        assertTrue(appellantInternalManageFeeUpdateLetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInternalManageFeeUpdateLetterPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantAddress is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantInternalManageFeeUpdateLetterPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

}