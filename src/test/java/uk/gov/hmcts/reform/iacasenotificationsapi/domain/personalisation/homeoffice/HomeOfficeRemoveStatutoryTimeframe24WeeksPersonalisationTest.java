package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

/**
 * Test class for HomeOfficeRemoveStatutoryTimeframe24WeeksPersonalisation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("PMD.TooManyFields")
class HomeOfficeRemoveStatutoryTimeframe24WeeksPersonalisationTest {

    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";
    private static final String ARIA_LISTING_REFERENCE_KEY = "ariaListingReference";
    private static final String HOME_OFFICE_REFERENCE_NUMBER_KEY = "homeOfficeReferenceNumber";
    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    private static final String LINK_TO_ONLINE_SERVICE_KEY = "linkToOnlineService";

    private static final String BEFORE_LISTING_TEMPLATE_ID = "beforeListingTemplateId";
    private static final String IA_EX_UI_FRONTEND_URL = "http://localhost";
    private static final HearingCentre HEARING_CENTRE = HearingCentre.TAYLOR_HOUSE;
    private static final String BEFORE_LISTING_EMAIL_ADDRESS = "homeoffice@example.com";
    private static final String AFTER_LISTING_EMAIL_ADDRESS = "hearinge@example.com";
    private static final String APPEAL_REFERENCE_NUMBER_VALUE = "someReferenceNumber";
    private static final String ARIA_LISTING_REFERENCE_VALUE = "someAriaListingReference";
    private static final String HOME_OFFICE_REF_NUMBER = "someHomeOfficeRefNumber";
    private static final String APPELLANT_GIVEN_NAMES_VALUE = "someAppellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_VALUE = "someAppellantFamilyName";
    private static final String CUSTOMER_SERVICES_TELEPHONE = "555 555 555";
    private static final String CUSTOMER_SERVICES_EMAIL = "cust.services@example.com";
    private static final String MOCK_PREFIX = "mox prefix";
    private static final Long CASE_ID = 12345L;
    private static final String EXPECTED_REFERENCE_ID = CASE_ID + "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL";

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private EmailAddressFinder emailAddressFinder;

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    private HomeOfficeRemoveStatutoryTimeframe24WeeksPersonalisation personalisation;

    @BeforeEach
    void setup() {
        setupAsylumCaseMocks();
        setupCustomerServicesMocks();

        personalisation = new HomeOfficeRemoveStatutoryTimeframe24WeeksPersonalisation(BEFORE_LISTING_TEMPLATE_ID, BEFORE_LISTING_EMAIL_ADDRESS, MOCK_PREFIX, IA_EX_UI_FRONTEND_URL, emailAddressFinder, customerServicesProvider);
    }

    @Test
    void shouldReturnGivenTemplateId() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertEquals(BEFORE_LISTING_TEMPLATE_ID, personalisation.getTemplateId(asylumCase));
    }

    @Test
    void shouldReturnGivenReferenceId() {
        assertEquals(EXPECTED_REFERENCE_ID, personalisation.getReferenceId(CASE_ID));
    }

    @Test
    void shouldReturnGivenEmailAddress() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        assertTrue(personalisation.getRecipientsList(asylumCase).contains(BEFORE_LISTING_EMAIL_ADDRESS));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HEARING_CENTRE));
        assertTrue(personalisation.getRecipientsList(asylumCase).contains(AFTER_LISTING_EMAIL_ADDRESS));
    }

    @Test
    void shouldThrowExceptionOnPersonalisationWhenCaseIsNull() {
        assertThatThrownBy(() -> personalisation.getPersonalisation((AsylumCase) null)).isExactlyInstanceOf(NullPointerException.class).hasMessage("asylumCase must not be null");
    }

    @Test
    void shouldReturnPersonalisationWhenAllInformationGiven() {
        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertPersonalisationContainsAllFields(result);
    }

    @Test
    void shouldReturnPersonalisationWhenAllMandatoryInformationGiven() {
        setupEmptyAsylumCaseMocks();

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertPersonalisationContainsMandatoryFields(result);
    }

    @Test
    void shouldReturnFalseIfAppealNotYetListed() {
        assertFalse(personalisation.isAppealListed(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        assertTrue(personalisation.isAppealListed(asylumCase));
    }

    private void setupAsylumCaseMocks() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ARIA_LISTING_REFERENCE_VALUE));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(APPELLANT_GIVEN_NAMES_VALUE));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(APPELLANT_FAMILY_NAME_VALUE));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(HOME_OFFICE_REF_NUMBER));
        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(AFTER_LISTING_EMAIL_ADDRESS);
    }

    private void setupEmptyAsylumCaseMocks() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
    }

    private void setupCustomerServicesMocks() {
        when(customerServicesProvider.getCustomerServicesTelephone()).thenReturn(CUSTOMER_SERVICES_TELEPHONE);
        when(customerServicesProvider.getCustomerServicesEmail()).thenReturn(CUSTOMER_SERVICES_EMAIL);
    }

    private void assertPersonalisationContainsAllFields(Map<String, String> personalisation) {
        assertEquals(APPEAL_REFERENCE_NUMBER_VALUE, personalisation.get(APPEAL_REFERENCE_NUMBER_KEY));
        assertEquals(ARIA_LISTING_REFERENCE_VALUE, personalisation.get(ARIA_LISTING_REFERENCE_KEY));
        assertEquals(HOME_OFFICE_REF_NUMBER, personalisation.get(HOME_OFFICE_REFERENCE_NUMBER_KEY));
        assertEquals(APPELLANT_GIVEN_NAMES_VALUE, personalisation.get(APPELLANT_GIVEN_NAMES_KEY));
        assertEquals(APPELLANT_FAMILY_NAME_VALUE, personalisation.get(APPELLANT_FAMILY_NAME_KEY));
        assertEquals(IA_EX_UI_FRONTEND_URL, personalisation.get(LINK_TO_ONLINE_SERVICE_KEY));
        assertEquals(CUSTOMER_SERVICES_TELEPHONE, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(CUSTOMER_SERVICES_EMAIL, customerServicesProvider.getCustomerServicesEmail());
    }

    private void assertPersonalisationContainsMandatoryFields(Map<String, String> personalisation) {
        assertEquals("", personalisation.get(APPEAL_REFERENCE_NUMBER_KEY));
        assertEquals("", personalisation.get(ARIA_LISTING_REFERENCE_KEY));
        assertEquals("", personalisation.get(HOME_OFFICE_REFERENCE_NUMBER_KEY));
        assertEquals("", personalisation.get(APPELLANT_GIVEN_NAMES_KEY));
        assertEquals("", personalisation.get(APPELLANT_FAMILY_NAME_KEY));
        assertEquals(IA_EX_UI_FRONTEND_URL, personalisation.get(LINK_TO_ONLINE_SERVICE_KEY));
        assertEquals(CUSTOMER_SERVICES_TELEPHONE, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(CUSTOMER_SERVICES_EMAIL, customerServicesProvider.getCustomerServicesEmail());
    }
}
