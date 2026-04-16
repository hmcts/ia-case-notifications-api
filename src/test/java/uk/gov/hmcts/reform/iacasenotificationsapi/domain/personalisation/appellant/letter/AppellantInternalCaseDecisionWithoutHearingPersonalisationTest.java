package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantInternalCaseDecisionWithoutHearingPersonalisationTest {

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

    private AppellantInternalCaseDecisionWithoutHearingPersonalisation appellantInternalCaseDecisionWithoutHearingPersonalisation;
    private final Long caseId = 12345L;
    private final String appellantInternalCaseDecisionWithoutHearingLetterTemplateId = "appellantInternalCaseDecisionWithoutHearingLetterTemplateId";
    private final String appealReferenceNumber = "someAppealRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String addressLine1 = "50";
    private final String addressLine2 = "Building name";
    private final String addressLine3 = "Street name";
    private final String postCode = "XX1 2YY";
    private final NationalityFieldValue oocAddressCountry = mock(NationalityFieldValue.class);
    private final String postTown = "Town name";
    private final String iaServicesPhone = "0300 123 1711";
    private final String iaServicesEmail = "contactia@justice.gov.uk";
    private final SystemDateProvider systemDateProvider = new SystemDateProvider();

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        String mockedAppealReferenceNumber = "someAppealRefNumber";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));
        String mockedAppellantGivenNames = "someAppellantGivenNames";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(mockedAppellantGivenNames));
        String mockedAppellantFamilyName = "someAppellantFamilyName";
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(mockedAppellantFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        String mockedAriaListingReference = "someAriaListingReference";
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(mockedAriaListingReference));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));

        appellantInternalCaseDecisionWithoutHearingPersonalisation = new AppellantInternalCaseDecisionWithoutHearingPersonalisation(
                appellantInternalCaseDecisionWithoutHearingLetterTemplateId,
                customerServicesProvider,
                systemDateProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(appellantInternalCaseDecisionWithoutHearingLetterTemplateId,
                appellantInternalCaseDecisionWithoutHearingPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_INTERNAL_DECISION_WITHOUT_HEARING_APPELLANT_LETTER",
                appellantInternalCaseDecisionWithoutHearingPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));


        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> appellantInternalCaseDecisionWithoutHearingPersonalisation.getRecipientsList(asylumCase))
            ;
assertEquals("appellantAddress is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_legalRep() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));


        IllegalStateException exception =
assertThrows(IllegalStateException.class, () -> appellantInternalCaseDecisionWithoutHearingPersonalisation.getRecipientsList(asylumCase))
            ;
assertEquals("legalRepAddressUK is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
assertThrows(NullPointerException.class,
                () -> appellantInternalCaseDecisionWithoutHearingPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
                ;
assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given_appellant_in_country() {
        appellantInCountryDataSetup();
        Map<String, String> personalisation =
            appellantInternalCaseDecisionWithoutHearingPersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("address_line_1", addressLine1)
            .containsEntry("address_line_2", addressLine2)
            .containsEntry("address_line_3", addressLine3)
            .containsEntry("address_line_4", postTown)
            .containsEntry("address_line_5", postCode);
    }

    @Test
    void should_return_personalisation_when_all_information_given_appellant_out_of_country() {
        appellantOutOfCountryDataSetup();
        Map<String, String> personalisation =
            appellantInternalCaseDecisionWithoutHearingPersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("address_line_1", addressLine1)
            .containsEntry("address_line_2", addressLine2)
            .containsEntry("address_line_3", addressLine3)
            .containsEntry("address_line_4", postTown)
            .containsEntry("address_line_5", Nationality.ES.toString());
    }

    @Test
    void should_return_personalisation_when_all_information_given_legalRep_in_country() {
        legalRepInCountryDataSetup();
        Map<String, String> personalisation =
            appellantInternalCaseDecisionWithoutHearingPersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("address_line_1", addressLine1)
            .containsEntry("address_line_2", addressLine2)
            .containsEntry("address_line_3", addressLine3)
            .containsEntry("address_line_4", postTown)
            .containsEntry("address_line_5", postCode);
    }

    @Test
    void should_return_personalisation_when_all_information_given_legalRep_out_of_country() {
        legalRepOutOfCountryDataSetup();
        Map<String, String> personalisation =
            appellantInternalCaseDecisionWithoutHearingPersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("address_line_1", addressLine1)
            .containsEntry("address_line_2", addressLine2)
            .containsEntry("address_line_3", addressLine3)
            .containsEntry("address_line_4", postTown);
        assertThat(personalisation).containsEntry("address_line_5", Nationality.ES.toString());
    }

    private void appellantOutOfCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(addressLine1));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(addressLine2));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(addressLine3));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_4_ADMIN_J, String.class)).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.COUNTRY_GOV_UK_OOC_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());
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
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_1, String.class)).thenReturn(Optional.of(addressLine1));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_2, String.class)).thenReturn(Optional.of(addressLine2));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_3, String.class)).thenReturn(Optional.of(addressLine3));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_4, String.class)).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.OOC_LR_COUNTRY_GOV_UK_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());
    }

    private void legalRepInCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.of(address));
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
    }
}
