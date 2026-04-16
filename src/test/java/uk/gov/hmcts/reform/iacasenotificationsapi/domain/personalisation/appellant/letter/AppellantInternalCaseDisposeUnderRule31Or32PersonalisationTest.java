package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantInternalCaseDisposeUnderRule31Or32PersonalisationTest {
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
    private final String postTown = "Town name";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "example@example.com";
    private final String oocAddressLine1 = "Calle Toledo 32";
    private final String oocAddressLine2 = "Madrid";
    private final String oocAddressLine3 = "28003";
    private final NationalityFieldValue oocAddressCountry = mock(NationalityFieldValue.class);
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
    private AppellantInternalCaseDisposeUnderRule31Or32Personalisation appellantInternalCaseDisposeUnderRule31Or32Personalisation;

    private static Stream<Arguments> getTestSource() {
        return Stream.of(
            Arguments.of(APPELLANT, YesOrNo.YES),
            Arguments.of(APPELLANT, YesOrNo.NO),
            Arguments.of(RESPONDENT, YesOrNo.YES),
            Arguments.of(RESPONDENT, YesOrNo.NO)
        );
    }

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
        String onlineCaseReferenceNumber = "1234 5678 9101 1121";
        when(asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.COUNTRY_GOV_UK_OOC_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());

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
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getRecipientsList(asylumCase));
        assertEquals("appellantAddress is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_legalRep_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getRecipientsList(asylumCase));
        assertEquals("legalRepAddressUK is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_applicant_type_is_not_present() {
        IllegalStateException exception =
            assertThrows(IllegalStateException.class,
                () -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation((callback)));
        assertEquals("ftpaApplicantType is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_appellant_decision_text_is_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(APPELLANT));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class,
                () -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation((callback)));
        assertEquals("ftpaAppellantDecisionRemadeRule32Text is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_respondent_decision_text_is_not_present() {
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(RESPONDENT));

        IllegalStateException exception =
            assertThrows(IllegalStateException.class,
                () -> appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation((callback)));
        assertEquals("ftpaRespondentDecisionRemadeRule32Text is not present", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("getTestSource")
    void should_return_personalisation_when_all_information_given_appellant_in_country(ApplicantType appellantType, YesOrNo appellantInUk) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(appellantType));
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_REMADE_RULE_32_TEXT, String.class)).thenReturn(Optional.of("test1"));
        when(asylumCase.read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_REMADE_RULE_32_TEXT, String.class)).thenReturn(Optional.of("test2"));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(appellantInUk));

        Map<String, String> personalisation =
            appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation(callback);

        if (appellantInUk == YesOrNo.YES) {
            assertThat(personalisation)
                .containsEntry("address_line_1", addressLine1)
                .containsEntry("address_line_2", addressLine2)
                .containsEntry("address_line_3", addressLine3)
                .containsEntry("address_line_4", postTown)
                .containsEntry("address_line_5", postCode);
        } else {
            assertThat(personalisation)
                .containsEntry("address_line_1", oocAddressLine1)
                .containsEntry("address_line_2", oocAddressLine2)
                .containsEntry("address_line_3", oocAddressLine3)
                .containsEntry("address_line_4", Nationality.ES.toString());
        }

        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

        if (appellantType == APPELLANT) {
            assertThat(personalisation)
                .containsEntry("applicant", "your")
                .containsEntry("ftpaDisposedReason", "test1");
        } else {
            assertThat(personalisation)
                .containsEntry("applicant", "the Home Office's")
                .containsEntry("ftpaDisposedReason", "test2");
        }
    }

    @ParameterizedTest
    @MethodSource("getTestSource")
    void should_return_personalisation_when_all_information_given_legalRep_in_country(ApplicantType appellantType, YesOrNo legalRepInUk) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(appellantType));
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_REMADE_RULE_32_TEXT, String.class)).thenReturn(Optional.of("test1"));
        when(asylumCase.read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_REMADE_RULE_32_TEXT, String.class)).thenReturn(Optional.of("test2"));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(legalRepInUk));

        if (legalRepInUk == YesOrNo.YES) {
            when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.of(address));
            when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
            when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
            when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
            when(address.getPostCode()).thenReturn(Optional.of(postCode));
            when(address.getPostTown()).thenReturn(Optional.of(postTown));
        } else {
            when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_1, String.class)).thenReturn(Optional.of(oocAddressLine1));
            when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_2, String.class)).thenReturn(Optional.of(oocAddressLine2));
            when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_3, String.class)).thenReturn(Optional.of(oocAddressLine3));
            when(asylumCase.read(AsylumCaseDefinition.OOC_LR_COUNTRY_GOV_UK_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        }

        Map<String, String> personalisation =
            appellantInternalCaseDisposeUnderRule31Or32Personalisation.getPersonalisation(callback);

        if (legalRepInUk == YesOrNo.YES) {
            assertThat(personalisation)
                .containsEntry("address_line_1", addressLine1)
                .containsEntry("address_line_2", addressLine2)
                .containsEntry("address_line_3", addressLine3)
                .containsEntry("address_line_4", postTown)
                .containsEntry("address_line_5", postCode);
        } else {
            assertThat(personalisation)
                .containsEntry("address_line_1", oocAddressLine1)
                .containsEntry("address_line_2", oocAddressLine2)
                .containsEntry("address_line_3", oocAddressLine3)
                .containsEntry("address_line_4", Nationality.ES.toString());
        }

        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

        if (appellantType == APPELLANT) {
            assertThat(personalisation)
                .containsEntry("applicant", "your")
                .containsEntry("ftpaDisposedReason", "test1");
        } else {
            assertThat(personalisation)
                .containsEntry("applicant", "the Home Office's")
                .containsEntry("ftpaDisposedReason", "test2");
        }
    }

}
