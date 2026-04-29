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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_14;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_42;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.HomeOfficeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisation.DAYS_56;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.D_MMM_YYYY;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("PMD.TooManyFields")
class AppellantCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationEmailTest {
    public static final String REVIEW_DATE = "2002-02-02";
    public static final String JUL_2002 = "20 Jul 2002";
    public static final String FEB_2002 = "2 Feb 2002";
    public static final String DAYS_42_FROM_DATE_OF_DIRECTION_KEY = "42DaysFromDateOfDirection";
    public static final String DECISION_SENT_DATE = "decisionSentDate";
    public static final String DECISION_SENT_DATE_KEY = DECISION_SENT_DATE;
    public static final String WEEKS_DEADLINE = "24WeeksDeadline";
    public static final String WEEKS_DEADLINE_KEY = WEEKS_DEADLINE;
    public static final String APPEAL_RECEIVED_DATE = "appealReceivedDate";
    public static final String APPEAL_RECEIVED_DATE_KEY = APPEAL_RECEIVED_DATE;
    public static final String PRACTICE_DIRECTION_KEY = "practiceDirection";
    public static final String DAYS_14_FROM_DATE_OF_DIRECTION_KEY = "14DaysFromDateOfDirection";
    public static final String DAYS_56_FROM_DATE_OF_DIRECTION = "56DaysFromDateOfDirection";
    public static final String FULL_NAME = "Given Family";
    public static final String APPELLANT_FULL_NAME_KEY = "appellantFullName";
    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    private static final String IA_EX_UI_FRONTEND_URL = "http://localhost";
    private static final String EMAIL_ADDRESS = "legal@example.com";
    private static final String APPELLANT_GIVEN_NAMES_VALUE = "Given";
    private static final String APPELLANT_FAMILY_NAME_VALUE = "Family";
    private static final String MOCK_PREFIX = "some mock prefix";
    private static final String CUSTOMER_SERVICES_EMAIL_KEY = "customerServicesEmail";
    private static final String CUSTOMER_SERVICES_TELEPHONE_KEY = "customerServicesTelephone";
    private static final String HOME_OFFICE_REFERENCE_NUMBER_VALUE = "323232";
    private static final String LINK_TO_SERVICE_KEY = "linkToService";
    private static final String EMAIL_TEMPLATE_ID = "someEmailTemplateId";
    private static final String CUSTOMER_SERVICE_PHONE = "123 454";
    private static final String CUSTOMER_SERVICE_EMAIL = "dummy@email.com";
    private static final Long CASE_ID = 12345L;
    private static final String EXPECTED_REFERENCE_ID =
            CASE_ID + "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL";
    @Mock
    private AsylumCase asylumCase;


    @Mock
    private CustomerServicesProvider customerServicesProvider;

    private AppellantCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationEmail personalisation;

    @BeforeEach
    void setup() {
        setupAsylumCaseMocks();
        setupCustomerServicesMocks();

        personalisation = new AppellantCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationEmail(
                EMAIL_TEMPLATE_ID,
                IA_EX_UI_FRONTEND_URL,
                MOCK_PREFIX,
                customerServicesProvider
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
    void shouldReturnGivenEmailAddress() {
        assertTrue(personalisation.getRecipientsList(asylumCase).contains(EMAIL_ADDRESS));
    }

    @Test
    void shouldReturnPersonalisationWhenAllInformationGiven() {
        Map<String, String> result = personalisation.getPersonalisation(asylumCase);
        assertEquals(APPELLANT_GIVEN_NAMES_VALUE, result.get(APPELLANT_GIVEN_NAMES_KEY));
        assertEquals(APPELLANT_FAMILY_NAME_VALUE, result.get(APPELLANT_FAMILY_NAME_KEY));
        assertEquals(FULL_NAME, result.get(APPELLANT_FULL_NAME_KEY));
        assertEquals(FEB_2002, result.get(DECISION_SENT_DATE_KEY));
        assertEquals(JUL_2002, result.get(WEEKS_DEADLINE_KEY));
        assertEquals(FEB_2002, result.get(APPEAL_RECEIVED_DATE_KEY));
        assertEquals(LocalDate.now().format(ofPattern(D_MMM_YYYY)), result.get(PRACTICE_DIRECTION_KEY));
        assertEquals(LocalDate.now().plusDays(DAYS_14).format(ofPattern(D_MMM_YYYY)), result.get(DAYS_14_FROM_DATE_OF_DIRECTION_KEY));
        assertEquals(LocalDate.now().plusDays(DAYS_42).format(ofPattern(D_MMM_YYYY)), result.get(DAYS_42_FROM_DATE_OF_DIRECTION_KEY));
        assertEquals(LocalDate.now().plusDays(DAYS_56).format(ofPattern(D_MMM_YYYY)), result.get(DAYS_56_FROM_DATE_OF_DIRECTION));
    }

    @Test
    void shouldReturnPersonalisationWhenAllMandatoryInformationGiven() {
        setupEmptyAsylumCaseMocks();

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertPersonalisationContainsMandatoryFields(result);
    }

    private void setupAsylumCaseMocks() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of(APPELLANT_FAMILY_NAME_VALUE));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of(APPELLANT_GIVEN_NAMES_VALUE));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(HOME_OFFICE_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(EMAIL, String.class))
                .thenReturn(Optional.of(EMAIL_ADDRESS));
        when(asylumCase.read(COMPLETE_CASE_REVIEW_DATE, String.class))
                .thenReturn(Optional.of(REVIEW_DATE));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.of(REVIEW_DATE));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of(REVIEW_DATE));
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
    }

    private void setupCustomerServicesMocks() {
        Map<String, String> customerServicesMap = new HashMap<>();
        customerServicesMap.put(CUSTOMER_SERVICES_TELEPHONE_KEY, CUSTOMER_SERVICE_PHONE);
        customerServicesMap.put(CUSTOMER_SERVICES_EMAIL_KEY, CUSTOMER_SERVICE_EMAIL);
        when(customerServicesProvider.getCustomerServicesPersonalisation())
                .thenReturn(customerServicesMap);
    }

    private void assertPersonalisationContainsMandatoryFields(Map<String, String> personalisation) {
        assertEquals("", personalisation.get(APPELLANT_GIVEN_NAMES_KEY));
        assertEquals("", personalisation.get(APPELLANT_FAMILY_NAME_KEY));
        assertEquals(IA_EX_UI_FRONTEND_URL, personalisation.get(LINK_TO_SERVICE_KEY));
    }
}
