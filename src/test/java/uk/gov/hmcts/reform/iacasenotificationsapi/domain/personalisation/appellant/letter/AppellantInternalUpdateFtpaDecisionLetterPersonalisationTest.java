package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantInternalUpdateFtpaDecisionLetterPersonalisationTest {

    private final Long caseId = 12345L;
    private final String templateId = "someLetterTemplateId";
    private final String appealReferenceNumber = "someAppealRefNumber";
    private final String homeOfficeReferenceNumber = "someHomeOfficeRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String addressLine1 = "50";
    private final String addressLine2 = "Building name";
    private final String addressLine3 = "Street name";
    private final String postCode = "XX1 2YY";
    private final String postTown = "Town name";
    private final String oocAddressLine1 = "Calle Toledo 32";
    private final String oocAddressLine2 = "Madrid";
    private final String oocAddressLine3 = "28003";
    private final NationalityFieldValue oocAddressCountry = mock(NationalityFieldValue.class);
    private final String appellantDisposedReason = "Appellant rule 31 reason";
    private final String respondentDisposedReason = "Respondent rule 31 reason";
    private final Map<String, String> customerServices = ImmutableMap.of(
        "customerServicesTelephone", "555 555 555",
        "customerServicesEmail", "cust.services@example.com"
    );

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AddressUk address;

    private AppellantInternalUpdateFtpaDecisionLetterPersonalisation personalisation;

    @BeforeEach
    void setUp() {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
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
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(customerServices);
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ApplicantType.APPELLANT));
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_REMADE_RULE_32_TEXT, String.class))
            .thenReturn(Optional.of(appellantDisposedReason));
        when(asylumCase.read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_REMADE_RULE_32_TEXT, String.class))
            .thenReturn(Optional.of(respondentDisposedReason));

        personalisation = new AppellantInternalUpdateFtpaDecisionLetterPersonalisation(templateId, customerServicesProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, personalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_UPDATE_FTPA_DECISION_LETTER", personalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_appellant_address_in_country_and_out_of_country() {
        appellantSubmittedSetup(YesOrNo.YES);
        assertThat(personalisation.getRecipientsList(asylumCase))
            .contains("someAppellantGivenNamessomeAppellantFamil_50_Buildingname_Streetname_Townname_XX12YY");

        appellantSubmittedSetup(YesOrNo.NO);
        assertThat(personalisation.getRecipientsList(asylumCase))
            .contains("someAppellantGivenNamessomeAppellantFamil_CalleToledo32_Madrid_28003_Spain");
    }

    @Test
    void should_return_legal_rep_address_when_submitted_as_legally_represented() {
        legalRepInCountrySetup();

        assertThat(personalisation.getRecipientsList(asylumCase)).contains("50_Buildingname_Streetname_Townname_XX12YY");
    }

    @Test
    void should_throw_when_appellant_address_missing() {
        appellantSubmittedSetup(YesOrNo.YES);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> personalisation.getRecipientsList(asylumCase));
        assertEquals("appellantAddress is not present", exception.getMessage());
    }

    @Test
    void should_return_personalisation_for_appellant_in_country() {
        appellantSubmittedSetup(YesOrNo.YES);

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsAllEntriesOf(customerServices)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("applicant", "your")
            .containsEntry("ftpaDisposedReason", appellantDisposedReason)
            .containsEntry("address_line_1", "someAppellantGivenNames someAppellantFamil")
            .containsEntry("address_line_2", addressLine1)
            .containsEntry("address_line_3", addressLine2)
            .containsEntry("address_line_4", addressLine3)
            .containsEntry("address_line_5", postTown)
            .containsEntry("address_line_6", postCode);
    }

    @Test
    void should_return_personalisation_for_appellant_out_of_country() {
        appellantSubmittedSetup(YesOrNo.NO);

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsEntry("address_line_1", "someAppellantGivenNames someAppellantFamil")
            .containsEntry("address_line_2", oocAddressLine1)
            .containsEntry("address_line_3", oocAddressLine2)
            .containsEntry("address_line_4", oocAddressLine3)
            .containsEntry("address_line_5", "Spain");
    }

    @Test
    void should_use_home_office_wording_and_reason_when_respondent_applied() {
        appellantSubmittedSetup(YesOrNo.YES);
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ApplicantType.RESPONDENT));

        assertThat(personalisation.getPersonalisation(asylumCase))
            .containsEntry("applicant", "the Home Office's")
            .containsEntry("ftpaDisposedReason", respondentDisposedReason);
    }

    @Test
    void should_return_empty_disposed_reason_when_not_present() {
        appellantSubmittedSetup(YesOrNo.YES);
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_REMADE_RULE_32_TEXT, String.class))
            .thenReturn(Optional.empty());

        assertThat(personalisation.getPersonalisation(asylumCase)).containsEntry("ftpaDisposedReason", "");
    }

    @Test
    void should_throw_when_ftpa_applicant_type_missing() {
        appellantSubmittedSetup(YesOrNo.YES);
        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> personalisation.getPersonalisation(asylumCase));
        assertEquals("ftpaApplicantType is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_asylum_case_is_null() {
        assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation((AsylumCase) null));
    }

    private void appellantSubmittedSetup(YesOrNo appellantInUk) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(appellantInUk));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(appellantInUk == YesOrNo.YES ? YesOrNo.NO : YesOrNo.YES));
    }

    private void legalRepInCountrySetup() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.of(address));
    }
}
