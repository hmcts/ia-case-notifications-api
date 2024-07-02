package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType.APPELLANT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType.RESPONDENT;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantInternalCaseDisposeUnderRule31Or32PersonalisationTest {
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
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "example@example.com";
    private String oocAddressLine1 = "Calle Toledo 32";
    private String oocAddressLine2 = "Madrid";
    private String oocAddressLine3 = "28003";
    private String oocAddressCountry = "Spain";
    private String onlineCaseReferenceNumber = "1234 5678 9101 1121";

    private AppellantInternalCaseDisposeUnderRule31Or32Personalisation appellantInternalCaseDisposeUnderRule31Or32Personalisation;

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
        when(asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
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

        appellantInternalCaseDisposeUnderRule31Or32Personalisation = new AppellantInternalCaseDisposeUnderRule31Or32Personalisation(
            letterTemplateId,
            customerServicesProvider
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, appellantInternalCaseDisposeUnderRule31Or32Personalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_INTERNAL_CASE_DISPOSE_UNDER_RULE_31_OR_32_APPELLANT_LETTER",
            appellantInternalCaseDisposeUnderRule31Or32Personalisation.getReferenceId(ccdCaseId));
    }


    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantAddress is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_throw_exception_on_personalisation_when_applicant_type_is_not_present() {
        assertThatThrownBy(
            () -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation((callback)))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaApplicantType is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_appellant_decision_text_is_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(APPELLANT));

        assertThatThrownBy(
            () -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation((callback)))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaAppellantDecisionRemadeRule32Text is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_respondent_decision_text_is_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(RESPONDENT));

        assertThatThrownBy(
            () -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation((callback)))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaRespondentDecisionRemadeRule32Text is not present");
    }

    @ParameterizedTest
    @MethodSource("getTestSource")
    void should_return_personalisation_when_all_information_given_in_country(ApplicantType appelantType, YesOrNo appellantInUk) {

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(appelantType));
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_REMADE_RULE_32_TEXT, String.class)).thenReturn(Optional.of("test1"));
        when(asylumCase.read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_REMADE_RULE_32_TEXT, String.class)).thenReturn(Optional.of("test2"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(appellantInUk));

        Map<String, String> personalisation =
            appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation(callback);

        if (appellantInUk == YesOrNo.YES) {
            assertEquals(addressLine1, personalisation.get("address_line_1"));
            assertEquals(addressLine2, personalisation.get("address_line_2"));
            assertEquals(addressLine3, personalisation.get("address_line_3"));
            assertEquals(postTown, personalisation.get("address_line_4"));
            assertEquals(postCode, personalisation.get("address_line_5"));
        } else {
            assertEquals(oocAddressLine1, personalisation.get("address_line_1"));
            assertEquals(oocAddressLine2, personalisation.get("address_line_2"));
            assertEquals(oocAddressLine3, personalisation.get("address_line_3"));
            assertEquals(oocAddressCountry, personalisation.get("address_line_4"));
        }

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

        if (appelantType == APPELLANT) {
            assertEquals("your", personalisation.get("applicant"));
            assertEquals("test1", personalisation.get("ftpaDisposedReason"));
        } else {
            assertEquals("the Home Office's", personalisation.get("applicant"));
            assertEquals("test2", personalisation.get("ftpaDisposedReason"));
        }
    }

    private static Stream<Arguments> getTestSource() {
        return Stream.of(
            Arguments.of(APPELLANT, YesOrNo.YES),
            Arguments.of(APPELLANT, YesOrNo.NO),
            Arguments.of(RESPONDENT, YesOrNo.YES),
            Arguments.of(RESPONDENT, YesOrNo.NO)
        );
    }

}