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

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("PMD.TooManyFields")
class HomeOfficeRemoveStatutoryTimeframe24WeeksPersonalisationTest {

    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    private static final String LINK_TO_ONLINE_SERVICE_KEY = "linkToOnlineService";
    private static final String HOME_OFFICE_REFERENCE_NUMBER_KEY = "homeOfficeReferenceNumber";
    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";
    private static final String BEFORE_LISTING_TEMPLATE_ID = "beforeListingTemplateId";
    private static final String IA_EX_UI_FRONTEND_URL = "http://localhost";
    private static final String BEFORE_LISTING_EMAIL_ADDRESS = "homeoffice@example.com";
    private static final String APPEAL_REFERENCE_NUMBER_VALUE = "someReferenceNumber";
    private static final String ARIA_LISTING_REFERENCE_VALUE = "someAriaListingReference";
    private static final String HOME_OFFICE_REF_NUMBER_VALUE = "someHomeOfficeRefNumber";
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
    private CustomerServicesProvider customerServicesProvider;

    private HomeOfficeRemoveStatutoryTimeframe24WeeksPersonalisation personalisation;

    @BeforeEach
    void setup() {
        setupAsylumCaseMocks();
        setupCustomerServicesMocks();

        personalisation = new HomeOfficeRemoveStatutoryTimeframe24WeeksPersonalisation(BEFORE_LISTING_TEMPLATE_ID, BEFORE_LISTING_EMAIL_ADDRESS, MOCK_PREFIX, IA_EX_UI_FRONTEND_URL, customerServicesProvider);
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
    void shouldThrowExceptionOnPersonalisationWhenCaseIsNull() {
        assertThatThrownBy(() -> personalisation.getPersonalisation((AsylumCase) null)).isExactlyInstanceOf(NullPointerException.class).hasMessage("asylumCase must not be null");
    }

    @Test
    void shouldReturnPersonalisationWhenAllInformationGiven() {
        Map<String, String> result = personalisation.getPersonalisation(asylumCase);
        assertEquals(HOME_OFFICE_REF_NUMBER_VALUE, result.get(HOME_OFFICE_REFERENCE_NUMBER_KEY));
        assertEquals(APPEAL_REFERENCE_NUMBER_VALUE, result.get(APPEAL_REFERENCE_NUMBER_KEY));

        assertEquals(APPELLANT_GIVEN_NAMES_VALUE, result.get(APPELLANT_GIVEN_NAMES_KEY));
        assertEquals(APPELLANT_FAMILY_NAME_VALUE, result.get(APPELLANT_FAMILY_NAME_KEY));
        assertEquals(IA_EX_UI_FRONTEND_URL, result.get(LINK_TO_ONLINE_SERVICE_KEY));
        assertEquals(CUSTOMER_SERVICES_TELEPHONE, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(CUSTOMER_SERVICES_EMAIL, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void shouldReturnPersonalisationWhenAllMandatoryInformationGiven() {
        setupEmptyAsylumCaseMocks();
        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertEquals("", result.get(APPELLANT_GIVEN_NAMES_KEY));
        assertEquals("", result.get(APPELLANT_FAMILY_NAME_KEY));
        assertEquals(IA_EX_UI_FRONTEND_URL, result.get(LINK_TO_ONLINE_SERVICE_KEY));
        assertEquals(CUSTOMER_SERVICES_TELEPHONE, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(CUSTOMER_SERVICES_EMAIL, customerServicesProvider.getCustomerServicesEmail());
    }

    private void setupAsylumCaseMocks() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ARIA_LISTING_REFERENCE_VALUE));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(APPELLANT_GIVEN_NAMES_VALUE));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(APPELLANT_FAMILY_NAME_VALUE));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(HOME_OFFICE_REF_NUMBER_VALUE));
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

}
