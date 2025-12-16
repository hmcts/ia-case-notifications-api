package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AddressUk sponsorAddress;

    private Long ccdCaseId = 12345L;
    private String letterTemplateId = "someLetterTemplateId";
    private String appealReferenceNumber = "someAppealRefNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String ccdReferenceNumber = "1234-1234-1234-1234";
    private String sponsorGivenNames = "someSponsorGivenNames";
    private String sponsorFamilyName = "someSponsorFamilyName";
    private String addressLine1 = "50";
    private String addressLine2 = "Building name";
    private String addressLine3 = "Street name";
    private String postCode = "XX1 2YY";
    private String postTown = "Town name";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "example@example.com";
    private final SystemDateProvider systemDateProvider = new SystemDateProvider();
    private int daysAfterSubmitAppeal = 14;
    private String amountLeftToPayInGbp = "140.00";
    private String feeAmountGbp = "14000";
    private SponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation sponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(ccdCaseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.SPONSOR_GIVEN_NAMES, String.class)).thenReturn(Optional.of(sponsorGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.SPONSOR_FAMILY_NAME, String.class)).thenReturn(Optional.of(sponsorFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.SPONSOR_ADDRESS, AddressUk.class)).thenReturn(Optional.of(sponsorAddress));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(sponsorAddress.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(sponsorAddress.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(sponsorAddress.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(sponsorAddress.getPostCode()).thenReturn(Optional.of(postCode));
        when(sponsorAddress.getPostTown()).thenReturn(Optional.of(postTown));

        sponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation = new SponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation(
            letterTemplateId,
            daysAfterSubmitAppeal,
            customerServicesProvider,
            systemDateProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, sponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_INTERNAL_SUBMIT_APPEAL_WITH_FEE_OUT_OF_TIME_SPONSOR_LETTER",
            sponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_sponsor_address_in_correct_format() {
        assertTrue(sponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));
    }

    @Test
    void should_throw_exception_when_cannot_find_sponsor_address() {
        when(asylumCase.read(AsylumCaseDefinition.SPONSOR_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("sponsor address is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_callback_is_null() {

        assertThatThrownBy(
            () -> sponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmountGbp));

        Map<String, String> personalisation =
            sponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation.getPersonalisation(callback);

        assertEquals(systemDateProvider.dueDate(14), personalisation.get("fourteenDaysAfterSubmitDate"));
        assertEquals(sponsorGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(sponsorFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(ccdReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(addressLine2, personalisation.get("address_line_2"));
        assertEquals(addressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(postCode, personalisation.get("address_line_5"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
    }

    @Test
    void should_return_personalisation_when_address_lines_are_missing() {
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmountGbp));
        when(sponsorAddress.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(sponsorAddress.getAddressLine2()).thenReturn(Optional.empty());
        when(sponsorAddress.getAddressLine3()).thenReturn(Optional.empty());
        when(sponsorAddress.getPostCode()).thenReturn(Optional.of(postCode));
        when(sponsorAddress.getPostTown()).thenReturn(Optional.of(postTown));

        Map<String, String> personalisation =
            sponsorInternalDetainedAppealSubmittedOutOfTimeWithFeePersonalisation.getPersonalisation(callback);

        assertEquals(systemDateProvider.dueDate(14), personalisation.get("fourteenDaysAfterSubmitDate"));
        assertEquals(sponsorGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(sponsorFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(ccdReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(postTown, personalisation.get("address_line_2"));
        assertEquals(postCode, personalisation.get("address_line_3"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
    }
}