package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("PMD.TooManyFields")
class AppellantRemoveStatutoryTimeframe24WeeksPersonalisationEmailTest {

    private static final String CUSTOMER_SERVICES_EMAIL_KEY = "customerServicesEmail";
    private static final String CUSTOMER_SERVICES_TELEPHONE_KEY = "customerServicesTelephone";
    private static final String SUBJECT_PREFIX_KEY = "subjectPrefix";
    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";
    private static final String LEGAL_REP_REFERENCE_NUMBER_KEY = "legalRepReferenceNumber";
    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    private static final String LINK_TO_ONLINE_SERVICE_KEY = "linkToOnlineService";

    private static final String MOCK_PREFIX = "Mock prefix";
    private static final String IA_EX_UI_FRONTEND_URL = "http://localhost";
    private static final String APPELLANT_GIVEN_NAMES = "appellant given names ";
    private static final String APPELLANT_FAMILY_NAME = "some family name";
    private static final String MOCKED_APPEAL_REFERENCE_NUMBER = "someReferenceNumber";
    private static final String LEGAL_REP_REF_NUMBER = "someLegalRefNumber";
    private static final String MOCKED_APPEAL_HOME_OFFICE_REFERENCE_NUMBER = "someHomeOfficeReferenceNumber";
    private static final String EMAIL_TEMPLATE_ID = "someEmailTemplateId";
    private static final String CUSTOMER_SERVICE_PHONE = "123 454";
    private static final String CUSTOMER_SERVICE_EMAIL = "dummy@email.com";
    private static final String MOCKED_APPELLANT_EMAIL_ADDRESS = "appelant@example.net";
    private static final Long CASE_ID = 12345L;
    private static final String EXPECTED_REFERENCE_ID =
            CASE_ID + "_REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL";

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private RecipientsFinder recipientsFinder;

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    private AppellantRemoveStatutoryTimeframe24WeeksPersonalisationEmail personalisation;

    @BeforeEach
    void setup() {
        setupAsylumCaseMocks();
        setupCustomerServicesMocks();

        personalisation = new AppellantRemoveStatutoryTimeframe24WeeksPersonalisationEmail(
                EMAIL_TEMPLATE_ID,
                IA_EX_UI_FRONTEND_URL,
                recipientsFinder,
                customerServicesProvider,
                MOCK_PREFIX
        );
    }

    @Test
    void shouldReturnGivenTemplateId() {
        assertEquals(EMAIL_TEMPLATE_ID, personalisation.getTemplateId());
    }

    @Test
    void shouldReturnGivenReferenceId() {
        assertEquals(EXPECTED_REFERENCE_ID, personalisation.getReferenceId(CASE_ID));
    }

    @Test
    void shouldReturnGivenEmailAddressListFromSubscribersInAsylumCase() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
                .thenReturn(Collections.singleton(MOCKED_APPELLANT_EMAIL_ADDRESS));

        assertTrue(personalisation.getRecipientsList(asylumCase)
                .contains(MOCKED_APPELLANT_EMAIL_ADDRESS));
    }

    @Test
    void shouldThrowExceptionOnPersonalisationWhenCaseIsNull() {
        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> personalisation.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
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

    private void setupAsylumCaseMocks() {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(MOCKED_APPEAL_REFERENCE_NUMBER));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(MOCKED_APPEAL_HOME_OFFICE_REFERENCE_NUMBER));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of(APPELLANT_FAMILY_NAME));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of(APPELLANT_GIVEN_NAMES));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(LEGAL_REP_REF_NUMBER));
    }

    private void setupEmptyAsylumCaseMocks() {
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());
    }

    private void setupCustomerServicesMocks() {
        Map<String, String> customerServicesMap = new HashMap<>();
        customerServicesMap.put(CUSTOMER_SERVICES_TELEPHONE_KEY, CUSTOMER_SERVICE_PHONE);
        customerServicesMap.put(CUSTOMER_SERVICES_EMAIL_KEY, CUSTOMER_SERVICE_EMAIL);
        when(customerServicesProvider.getCustomerServicesPersonalisation())
                .thenReturn(customerServicesMap);
    }

    private void assertPersonalisationContainsAllFields(Map<String, String> personalisation) {
        assertEquals(MOCK_PREFIX, personalisation.get(SUBJECT_PREFIX_KEY));
        assertEquals(MOCKED_APPEAL_REFERENCE_NUMBER, personalisation.get(APPEAL_REFERENCE_NUMBER_KEY));
        assertEquals(LEGAL_REP_REF_NUMBER, personalisation.get(LEGAL_REP_REFERENCE_NUMBER_KEY));
        assertEquals(APPELLANT_GIVEN_NAMES, personalisation.get(APPELLANT_GIVEN_NAMES_KEY));
        assertEquals(APPELLANT_FAMILY_NAME, personalisation.get(APPELLANT_FAMILY_NAME_KEY));
        assertEquals(IA_EX_UI_FRONTEND_URL, personalisation.get(LINK_TO_ONLINE_SERVICE_KEY));
        assertEquals(CUSTOMER_SERVICE_EMAIL, personalisation.get(CUSTOMER_SERVICES_EMAIL_KEY));
        assertEquals(CUSTOMER_SERVICE_PHONE, personalisation.get(CUSTOMER_SERVICES_TELEPHONE_KEY));
    }

    private void assertPersonalisationContainsMandatoryFields(Map<String, String> personalisation) {
        assertEquals("", personalisation.get(APPEAL_REFERENCE_NUMBER_KEY));
        assertEquals("", personalisation.get(LEGAL_REP_REFERENCE_NUMBER_KEY));
        assertEquals("", personalisation.get(APPELLANT_GIVEN_NAMES_KEY));
        assertEquals("", personalisation.get(APPELLANT_FAMILY_NAME_KEY));
        assertEquals(IA_EX_UI_FRONTEND_URL, personalisation.get(LINK_TO_ONLINE_SERVICE_KEY));
    }
}
