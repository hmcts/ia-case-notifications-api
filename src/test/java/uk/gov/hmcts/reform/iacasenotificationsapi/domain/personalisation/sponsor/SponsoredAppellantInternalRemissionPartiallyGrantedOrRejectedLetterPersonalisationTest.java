package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.sponsor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SponsoredAppellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    AddressUk sponsorAddress;

    private final Long ccdCaseId = 12345L;

    private final String letterTemplateId = "someLetterTemplateId";
    private final int daysAfterRemissionDecision = 14;

    private final String appealReferenceNumber = "someAppealRefNumber";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String remissionReasons = "someTestReason";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String ccdReferenceNumber = "1234-1234-1234-1234";

    private final String addressLine1 = "50";
    private final String addressLine2 = "Building name";
    private final String addressLine3 = "Street name";
    private final String postCode = "XX1 2YY";
    private final String postTown = "Town name";

    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "example@example.com";

    private final String amountLeftToPay = "4000";
    private final String amountLeftToPayInGbp = "40.00";
    private final String feeAmountGbp = "18000";
    private final String feeAmountGbpInGbp = "180.00";

    private final String dueDate = "1 Jan 2026";

    private SponsoredAppellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation personalisation;

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(ccdCaseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of(remissionReasons));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumber));

        when(asylumCase.read(AsylumCaseDefinition.SPONSOR_ADDRESS, AddressUk.class)).thenReturn(Optional.of(sponsorAddress));
        when(sponsorAddress.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(sponsorAddress.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(sponsorAddress.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(sponsorAddress.getPostCode()).thenReturn(Optional.of(postCode));
        when(sponsorAddress.getPostTown()).thenReturn(Optional.of(postTown));

        when(customerServicesProvider.getCustomerServicesPersonalisation())
                .thenReturn(Map.of(
                        "customerServicesTelephone", customerServicesTelephone,
                        "customerServicesEmail", customerServicesEmail
                ));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        when(asylumCase.read(AsylumCaseDefinition.AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when(asylumCase.read(AsylumCaseDefinition.FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmountGbp));

        personalisation = new SponsoredAppellantInternalRemissionPartiallyGrantedOrRejectedLetterPersonalisation(
                letterTemplateId,
                daysAfterRemissionDecision,
                customerServicesProvider,
                systemDateProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, personalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(
                ccdCaseId + "_INTERNAL_REMISSION_PARTIALLY_GRANTED_REFUSED_SPONSOR_LETTER",
                personalisation.getReferenceId(ccdCaseId)
        );
    }

    @Test
    void should_return_sponsor_address_in_correct_format() {
        assertTrue(personalisation.getRecipientsList(asylumCase)
                .contains("50_Buildingname_Streetname_Townname_XX12YY"));
    }

    @Test
    void should_throw_exception_when_cannot_find_sponsor_address() {
        when(asylumCase.read(AsylumCaseDefinition.SPONSOR_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalisation.getRecipientsList(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("sponsor address is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_callback_is_null() {
        assertThatThrownBy(() -> personalisation.getPersonalisation((Callback<AsylumCase>) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("callback must not be null");
    }

    @Test
    void should_return_personalisation_when_partially_approved() {
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));

        Map<String, String> map = personalisation.getPersonalisation(callback);

        assertEquals(appealReferenceNumber, map.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, map.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, map.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, map.get("appellantFamilyName"));
        assertEquals(remissionReasons, map.get("RemissionReasons"));
        assertEquals(ccdReferenceNumber, map.get("onlineCaseReferenceNumber"));

        assertEquals(dueDate, map.get("payByDeadline"));

        assertEquals(addressLine1, map.get("address_line_1"));
        assertEquals(addressLine2, map.get("address_line_2"));
        assertEquals(addressLine3, map.get("address_line_3"));
        assertEquals(postTown, map.get("address_line_4"));
        assertEquals(postCode, map.get("address_line_5"));

        assertEquals(customerServicesTelephone, map.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, map.get("customerServicesEmail"));

        assertEquals(amountLeftToPayInGbp, map.get("feeAmount"));
    }

    @Test
    void should_return_personalisation_when_rejected() {
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.REJECTED));

        Map<String, String> map = personalisation.getPersonalisation(callback);

        assertEquals(feeAmountGbpInGbp, map.get("feeAmount"));
    }
}
