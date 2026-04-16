package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplicationTypes.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantInternalRespondentApplicationDecidedLetterPersonalisationTest {

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
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    MakeAnApplication makeAnApplication;

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
    private final String applicationGranted = "Granted";
    private final String applicationRefused = "Refused";
    private final int daysAfterApplicationDecisionInCountry = 14;
    private final int daysAfterApplicationDecisionOoc = 28;

    private static final String HOME_OFFICE_TIME_EXTENTION_CONTENT = "The tribunal will give the Home Office more time to complete its next task. You will get a notification with the new date soon.";
    private static final String HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT = "The details of the hearing will be updated and you will be sent a new Notice of Hearing with the agreed changes.";
    private static final String HOME_OFFICE_JUDGES_REVIEW_CONTENT = "The decision on the Home Office’s original request will be overturned. You will be notified if there is something you need to do next.";
    private static final String HOME_OFFICE_LINK_OR_UNLINK_CONTENT = "This appeal will be linked to or unlinked from the appeal in the Home Office application. You will be notified when this happens.";
    private static final String HOME_OFFICE_REINSTATE_APPEAL_CONTENT = "This appeal will be reinstated and will continue from the point where it was ended. You will be notified when this happens.";
    private static final String APPLICATION_TYPE_OTHER_CONTENT = "You will be notified when the tribunal makes the changes the Home Office asked for.";
    private static final String HOME_OFFICE_REFUSED_CONTENT = "The appeal will continue without any changes.";

    private AppellantInternalRespondentApplicationDecidedLetterPersonalisation appellantInternalRespondentApplicationDecidedLetterPersonalisation;

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
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.COUNTRY_GOV_UK_OOC_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.ofNullable(makeAnApplication));
        String applicationReason = "Application decision reason example";
        when(makeAnApplication.getDecisionReason()).thenReturn(applicationReason);
        final String dueDate = LocalDate.now().plusDays(daysAfterApplicationDecisionInCountry)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterApplicationDecisionInCountry)).thenReturn(dueDate);

        appellantInternalRespondentApplicationDecidedLetterPersonalisation = new AppellantInternalRespondentApplicationDecidedLetterPersonalisation(
            letterTemplateId,
            daysAfterApplicationDecisionInCountry,
            daysAfterApplicationDecisionOoc,
            customerServicesProvider,
            systemDateProvider,
            makeAnApplicationService
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, appellantInternalRespondentApplicationDecidedLetterPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_INTERNAL_RESPONDENT_APPLICATION_DECIDED_APPELLANT_LETTER",
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_appellant_address_in_correct_format() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertTrue(appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase).contains("CalleToledo32_Madrid_28003_Spain"));
    }

    @Test
    void should_return_legalRep_address_in_correct_format() {
        legalRepInCountryDataSetup();
        assertTrue(appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));

        legalRepOutOfCountryDataSetup();
        assertTrue(appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase).contains("CalleToledo32_Madrid_28003_Townname_Spain"));
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase));
        assertEquals("appellantAddress is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_legalRep_in_country() {
        legalRepInCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.empty());

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase));
        assertEquals("legalRepAddressUK is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation((Callback<AsylumCase>) null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(
        value = MakeAnApplicationTypes.class,
        names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
            "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_appellant_in_country_granted_application(MakeAnApplicationTypes applicationType) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationGranted);
        Map<String, String> personalisation =
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

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
        if (applicationType == ADJOURN || applicationType == EXPEDITE || applicationType == TRANSFER) {
            assertEquals(HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == TIME_EXTENSION) {
            assertEquals(HOME_OFFICE_TIME_EXTENTION_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == JUDGE_REVIEW_LO) {
            assertEquals(HOME_OFFICE_JUDGES_REVIEW_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == LINK_OR_UNLINK) {
            assertEquals(HOME_OFFICE_LINK_OR_UNLINK_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == REINSTATE) {
            assertEquals(HOME_OFFICE_REINSTATE_APPEAL_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == WITHDRAW) {
            assertEquals("Your appeal may end. If you do not want the appeal to end, you should contact the tribunal by " +
                systemDateProvider.dueDate(daysAfterApplicationDecisionInCountry) + " to explain why.", personalisation.get("nextStep"));
        } else if (applicationType == OTHER) {
            assertEquals(APPLICATION_TYPE_OTHER_CONTENT, personalisation.get("nextStep"));
        }
    }

    @ParameterizedTest
    @EnumSource(
        value = MakeAnApplicationTypes.class,
        names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
            "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_appellant_out_of_country_granted_application(MakeAnApplicationTypes applicationType) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationGranted);
        final String dueDate = LocalDate.now().plusDays(daysAfterApplicationDecisionOoc)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterApplicationDecisionOoc)).thenReturn(dueDate);
        Map<String, String> personalisation =
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", oocAddressLine1)
            .containsEntry("address_line_2", oocAddressLine2)
            .containsEntry("address_line_3", oocAddressLine3)
            .containsEntry("address_line_4", Nationality.ES.toString());
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        if (applicationType == ADJOURN || applicationType == EXPEDITE || applicationType == TRANSFER) {
            assertEquals(HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == TIME_EXTENSION) {
            assertEquals(HOME_OFFICE_TIME_EXTENTION_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == JUDGE_REVIEW_LO) {
            assertEquals(HOME_OFFICE_JUDGES_REVIEW_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == LINK_OR_UNLINK) {
            assertEquals(HOME_OFFICE_LINK_OR_UNLINK_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == REINSTATE) {
            assertEquals(HOME_OFFICE_REINSTATE_APPEAL_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == WITHDRAW) {
            assertEquals("Your appeal may end. If you do not want the appeal to end, you should contact the tribunal by " +
                systemDateProvider.dueDate(daysAfterApplicationDecisionOoc) + " to explain why.", personalisation.get("nextStep"));
        } else if (applicationType == OTHER) {
            assertEquals(APPLICATION_TYPE_OTHER_CONTENT, personalisation.get("nextStep"));
        }
    }

    @ParameterizedTest
    @EnumSource(
        value = MakeAnApplicationTypes.class,
        names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
            "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_appellant_refused_application(MakeAnApplicationTypes applicationType) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationRefused);
        Map<String, String> personalisation =
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(HOME_OFFICE_REFUSED_CONTENT, personalisation.get("nextStep"));

    }

    @ParameterizedTest
    @EnumSource(
        value = MakeAnApplicationTypes.class,
        names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
            "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_legalRep_in_country_granted_application(MakeAnApplicationTypes applicationType) {
        legalRepInCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationGranted);
        Map<String, String> personalisation =
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

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
        if (applicationType == ADJOURN || applicationType == EXPEDITE || applicationType == TRANSFER) {
            assertEquals(HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == TIME_EXTENSION) {
            assertEquals(HOME_OFFICE_TIME_EXTENTION_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == JUDGE_REVIEW_LO) {
            assertEquals(HOME_OFFICE_JUDGES_REVIEW_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == LINK_OR_UNLINK) {
            assertEquals(HOME_OFFICE_LINK_OR_UNLINK_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == REINSTATE) {
            assertEquals(HOME_OFFICE_REINSTATE_APPEAL_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == WITHDRAW) {
            assertEquals("Your appeal may end. If you do not want the appeal to end, you should contact the tribunal by " +
                systemDateProvider.dueDate(daysAfterApplicationDecisionInCountry) + " to explain why.", personalisation.get("nextStep"));
        } else if (applicationType == OTHER) {
            assertEquals(APPLICATION_TYPE_OTHER_CONTENT, personalisation.get("nextStep"));
        }
    }

    @ParameterizedTest
    @EnumSource(
        value = MakeAnApplicationTypes.class,
        names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
            "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_legalRep_out_of_country_granted_application(MakeAnApplicationTypes applicationType) {
        legalRepOutOfCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationGranted);
        final String dueDate = LocalDate.now().plusDays(daysAfterApplicationDecisionOoc)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterApplicationDecisionOoc)).thenReturn(dueDate);
        Map<String, String> personalisation =
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("address_line_1", oocAddressLine1)
            .containsEntry("address_line_2", oocAddressLine2)
            .containsEntry("address_line_3", oocAddressLine3)
            .containsEntry("address_line_4", postTown)
            .containsEntry("address_line_5", Nationality.ES.toString());
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        if (applicationType == ADJOURN || applicationType == EXPEDITE || applicationType == TRANSFER) {
            assertEquals(HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == TIME_EXTENSION) {
            assertEquals(HOME_OFFICE_TIME_EXTENTION_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == JUDGE_REVIEW_LO) {
            assertEquals(HOME_OFFICE_JUDGES_REVIEW_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == LINK_OR_UNLINK) {
            assertEquals(HOME_OFFICE_LINK_OR_UNLINK_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == REINSTATE) {
            assertEquals(HOME_OFFICE_REINSTATE_APPEAL_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == WITHDRAW) {
            assertEquals("Your appeal may end. If you do not want the appeal to end, you should contact the tribunal by " +
                systemDateProvider.dueDate(daysAfterApplicationDecisionOoc) + " to explain why.", personalisation.get("nextStep"));
        } else if (applicationType == OTHER) {
            assertEquals(APPLICATION_TYPE_OTHER_CONTENT, personalisation.get("nextStep"));
        }
    }

    @ParameterizedTest
    @EnumSource(
        value = MakeAnApplicationTypes.class,
        names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
            "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_legalRep_refused_application(MakeAnApplicationTypes applicationType) {
        legalRepInCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationRefused);
        Map<String, String> personalisation =
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(HOME_OFFICE_REFUSED_CONTENT, personalisation.get("nextStep"));

    }

    @Test
    void should_throw_exception_when_cannot_find_next_step() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(makeAnApplication.getType()).thenReturn(UPDATE_APPEAL_DETAILS.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationGranted);

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback));
        assertEquals("Invalid MakeAnApplicationType: Couldn't find next steps.", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_cannot_find_application() {
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.empty());

        RequiredFieldMissingException exception =
            assertThrows(RequiredFieldMissingException.class, () -> appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback));
        assertEquals("Application is missing.", exception.getMessage());
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
