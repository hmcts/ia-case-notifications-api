package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantInternalReinstateAppealLetterPersonalisationTest {
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
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String addressLine1 = "50";
    private String addressLine2 = "Building name";
    private String addressLine3 = "Street name";
    private String postCode = "XX1 2YY";
    private String postTown = "Town name";
    private String oocAddressLine1 = "Calle Toledo 32";
    private String oocAddressLine2 = "Madrid";
    private String oocAddressLine3 = "28003";
    private String oocAddressCountry = "Spain";
    private String decisionMaker = "Legal Officer";
    private String reinstateReason = "Example reason";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "example@example.com";

    private AppellantInternalReinstateAppealLetterPersonalisation appellantInternalReinstateAppealLetterPersonalisation;

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
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(appellantAddress.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(appellantAddress.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(appellantAddress.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(appellantAddress.getPostCode()).thenReturn(Optional.of(postCode));
        when(appellantAddress.getPostTown()).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.COUNTRY_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressCountry));
        when(asylumCase.read(AsylumCaseDefinition.REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.of(decisionMaker));
        when(asylumCase.read(AsylumCaseDefinition.REINSTATE_APPEAL_REASON, String.class)).thenReturn(Optional.of(reinstateReason));


        appellantInternalReinstateAppealLetterPersonalisation = new AppellantInternalReinstateAppealLetterPersonalisation(
            letterTemplateId,
            customerServicesProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, appellantInternalReinstateAppealLetterPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_INTERNAL_REINSTATE_APPEAL_APPELLANT_LETTER",
            appellantInternalReinstateAppealLetterPersonalisation.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_address_in_correct_format() {

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(appellantInternalReinstateAppealLetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertTrue(appellantInternalReinstateAppealLetterPersonalisation.getRecipientsList(asylumCase).contains("CalleToledo32_Madrid_28003_Spain"));
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInternalReinstateAppealLetterPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantAddress is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantInternalReinstateAppealLetterPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given_in_country() {

        String reinstateAppealDate = LocalDate.now().toString();
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.of(reinstateAppealDate));

        Map<String, String> personalisation =
            appellantInternalReinstateAppealLetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(addressLine2, personalisation.get("address_line_2"));
        assertEquals(addressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(postCode, personalisation.get("address_line_5"));
        assertEquals(LocalDate.parse(reinstateAppealDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")), personalisation.get("reinstateAppealDate"));
        assertEquals(decisionMaker, personalisation.get("decisionMaker"));
        assertEquals(reinstateReason, personalisation.get("reinstateReason"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void should_return_personalisation_when_all_information_given_out_of_country() {

        String reinstateAppealDate = LocalDate.now().toString();
        when(asylumCase.read(AsylumCaseDefinition.REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.of(reinstateAppealDate));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Map<String, String> personalisation =
            appellantInternalReinstateAppealLetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(oocAddressLine1, personalisation.get("address_line_1"));
        assertEquals(oocAddressLine2, personalisation.get("address_line_2"));
        assertEquals(oocAddressLine3, personalisation.get("address_line_3"));
        assertEquals(oocAddressCountry, personalisation.get("address_line_4"));
        assertEquals(LocalDate.parse(reinstateAppealDate).format(DateTimeFormatter.ofPattern("d MMM yyyy")), personalisation.get("reinstateAppealDate"));
        assertEquals(decisionMaker, personalisation.get("decisionMaker"));
        assertEquals(reinstateReason, personalisation.get("reinstateReason"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
