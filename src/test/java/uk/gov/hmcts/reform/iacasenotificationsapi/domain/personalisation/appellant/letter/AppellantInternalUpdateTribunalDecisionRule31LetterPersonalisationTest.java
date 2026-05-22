package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TYPES_OF_UPDATE_TRIBUNAL_DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.UPDATED_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantInternalUpdateTribunalDecisionRule31LetterPersonalisationTest {
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
    private final String oocAddressLine1 = "Calle Toledo 32";
    private final String oocAddressLine2 = "Madrid";
    private final String oocAddressLine3 = "28003";
    private final NationalityFieldValue oocAddressCountry = mock(NationalityFieldValue.class);
    private final String decisionMaker = "Legal Officer";
    private final String reinstateReason = "Example reason";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "example@example.com";
    private final String allowed = "Allowed";
    private final String dismissed = "Dismissed";
    private final DynamicList dynamicAllowedDecisionList = new DynamicList(
        new Value("allowed", "Yes, change decision to Allowed"),
        newArrayList()
    );
    private final DynamicList dynamicDismissedDecisionList = new DynamicList(
        new Value("dismissed", "No"),
        newArrayList()
    );
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
    private AppellantInternalUpdateTribunalDecisionRule31LetterPersonalisation appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation;


    @BeforeEach
    void setUp() {
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
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.COUNTRY_GOV_UK_OOC_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicDismissedDecisionList));

        appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation = new AppellantInternalUpdateTribunalDecisionRule31LetterPersonalisation(
            letterTemplateId, customerServicesProvider);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_INTERNAL_UPDATE_TRIBUNAL_DECISION_RULE31_LETTER",
            appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_appellant_address_in_correct_format() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase)
                .contains("someAppellantGivenNamessomeAppellantFamil_50_Buildingname_Streetname_Townname_XX12YY"));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertTrue(appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase)
                .contains("someAppellantGivenNamessomeAppellantFamil_CalleToledo32_Madrid_28003_Spain"));
    }

    @Test
    void should_return_legalRep_address_in_correct_format() {
        legalRepInCountryDataSetup();
        assertTrue(appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));

        legalRepOutOfCountryDataSetup();
        assertTrue(appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase).contains("CalleToledo32_Madrid_28003_Townname_Spain"));
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase));
        assertEquals("appellantAddress is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_legalRep_in_country() {
        legalRepInCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getRecipientsList(asylumCase));
        assertEquals("legalRepAddressUK is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({allowed, dismissed})
    void should_return_personalisation_when_all_information_given_appellant_in_country(String decision) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(decision));
        Map<String, String> personalisation =
            appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);
        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", (appellantGivenNames + " " + appellantFamilyName).substring(0, 42))
            .containsEntry("address_line_2", addressLine1)
            .containsEntry("address_line_3", addressLine2)
            .containsEntry("address_line_4", addressLine3)
            .containsEntry("address_line_5", postTown)
            .containsEntry("address_line_6", postCode)
            .containsEntry("oldDecision", decision.equals(allowed) ? dismissed : allowed)
            .containsEntry("newDecision", decision)
            .containsEntry("period", "14 days");

    }

    @ParameterizedTest
    @CsvSource({allowed, dismissed})
    void should_return_personalisation_when_all_information_given_appellant_ooc(String decision) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(decision));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        Map<String, String> personalisation =
            appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);
        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", (appellantGivenNames + " " + appellantFamilyName).substring(0, 42))
            .containsEntry("address_line_2", oocAddressLine1)
            .containsEntry("address_line_3", oocAddressLine2)
            .containsEntry("address_line_4", oocAddressLine3)
            .containsEntry("address_line_5", Nationality.ES.toString())
            .containsEntry("oldDecision", decision.equals(allowed) ? dismissed : allowed)
            .containsEntry("newDecision", decision)
            .containsEntry("period", "28 days");
    }

    @Test
    void should_return_personalisation_when_all_information_given_appellant_decision_not_updated() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicDismissedDecisionList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of("Allowed"));
        Map<String, String> personalisation = appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", (appellantGivenNames + " " + appellantFamilyName).substring(0, 42))
            .containsEntry("address_line_2", oocAddressLine1)
            .containsEntry("address_line_3", oocAddressLine2)
            .containsEntry("address_line_4", oocAddressLine3)
            .containsEntry("address_line_5", Nationality.ES.toString());
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertNull(personalisation.get("oldDecision"));
        assertNull(personalisation.get("newDecision"));
        assertNull(personalisation.get("period"));
    }

    @ParameterizedTest
    @CsvSource({allowed, dismissed})
    void should_return_personalisation_when_all_information_given_legalRep_in_country(String decision) {
        legalRepInCountryDataSetup();
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(decision));
        Map<String, String> personalisation =
            appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);
        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", addressLine1)
            .containsEntry("address_line_2", addressLine2)
            .containsEntry("address_line_3", addressLine3)
            .containsEntry("address_line_4", postTown)
            .containsEntry("address_line_5", postCode)
            .containsEntry("oldDecision", decision.equals(allowed) ? dismissed : allowed)
            .containsEntry("newDecision", decision)
            .containsEntry("period", "14 days");

    }

    @ParameterizedTest
    @CsvSource({allowed, dismissed})
    void should_return_personalisation_when_all_information_given_legalRep_ooc(String decision) {
        legalRepOutOfCountryDataSetup();
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicAllowedDecisionList));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(decision));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        Map<String, String> personalisation =
            appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);
        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", oocAddressLine1)
            .containsEntry("address_line_2", oocAddressLine2)
            .containsEntry("address_line_3", oocAddressLine3)
            .containsEntry("address_line_4", postTown)
            .containsEntry("address_line_5", Nationality.ES.toString())
            .containsEntry("oldDecision", decision.equals(allowed) ? dismissed : allowed)
            .containsEntry("newDecision", decision)
            .containsEntry("period", "28 days");
    }

    @Test
    void should_return_personalisation_when_all_information_given_legalRep_decision_not_updated() {
        legalRepInCountryDataSetup();
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
            .thenReturn(Optional.of(dynamicDismissedDecisionList));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of("Allowed"));
        Map<String, String> personalisation = appellantInternalUpdateTribunalDecisionRule31LetterPersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
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
        assertNull(personalisation.get("oldDecision"));
        assertNull(personalisation.get("newDecision"));
        assertNull(personalisation.get("period"));
    }

    private void legalRepOutOfCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_1, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_2, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_3, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_4, String.class)).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.OOC_LR_COUNTRY_GOV_UK_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());
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
