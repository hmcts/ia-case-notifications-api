package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.D_MMM_YYYY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.APPEAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_14;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_14_FROM_DATE_OF_DIRECTION_KEY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_42;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_42_FROM_DATE_OF_DIRECTION_KEY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_56;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DAYS_56_FROM_DATE_OF_DIRECTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.DECISION_SENT_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.PRACTICE_DIRECTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.WEEKS_DEADLINE;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("PMD.TooManyFields")
class LegalRepresentativeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationLetterTest {

    private static final String REVIEW_DATE = "2002-02-02";
    private static final String JUL_2002 = "20 Jul 2002";
    private static final String FEB_2002 = "2 Feb 2002";
    private static final String LETTER_TEMPLATE_ID = "letterTemplate123";
    private static final String APPEAL_REFERENCE_NUMBER_VALUE = "PA/12345/2024";
    private static final String LEGAL_REP_REFERENCE_NUMBER_VALUE = "LR-REF-001";
    private static final String APPELLANT_GIVEN_NAMES_VALUE = "John";
    private static final String APPELLANT_FAMILY_NAME_VALUE = "Smith";
    private static final String HOME_OFFICE_REFERENCE_NUMBER_VALUE = "HO-REF-001";
    private static final String CUSTOMER_SERVICES_EMAIL_KEY = "customerServicesEmail";
    private static final String CUSTOMER_SERVICES_TELEPHONE_KEY = "customerServicesTelephone";
    private static final String CUSTOMER_SERVICE_PHONE = "0300 123 1711";
    private static final String CUSTOMER_SERVICE_EMAIL = "customer@example.com";
    private static final Long CASE_ID = 12345L;
    private static final String EXPECTED_REFERENCE_ID =
            CASE_ID + STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_LETTER;

    private static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";
    private static final String LEGAL_REP_REFERENCE_NUMBER_KEY = "legalRepReferenceNumber";
    private static final String APPELLANT_GIVEN_NAMES_KEY = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_KEY = "appellantFamilyName";
    private static final String HOME_OFFICE_REFERENCE_NUMBER_KEY = "homeOfficeReferenceNumber";
    private static final String DECISION_SENT_DATE_KEY = DECISION_SENT_DATE;
    private static final String WEEKS_DEADLINE_KEY = WEEKS_DEADLINE;
    private static final String APPEAL_RECEIVED_DATE_KEY = APPEAL_RECEIVED_DATE;
    private static final String PRACTICE_DIRECTION_KEY = PRACTICE_DIRECTION;

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    private LegalRepresentativeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationLetter personalisation;

    @BeforeEach
    void setup() {
        setupAsylumCaseMocks();
        setupCustomerServicesMocks();

        personalisation = new LegalRepresentativeCompleteCaseReviewStatutoryTimeframe24WeeksPersonalisationLetter(
                LETTER_TEMPLATE_ID,
                customerServicesProvider
        );
    }

    @Test
    void shouldReturnTemplateId() {
        assertEquals(LETTER_TEMPLATE_ID, personalisation.getTemplateId());
    }

    @Test
    void shouldReturnReferenceId() {
        assertEquals(EXPECTED_REFERENCE_ID, personalisation.getReferenceId(CASE_ID));
    }

    @Test
    void shouldReturnRecipientsList() {
        assertFalse(personalisation.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    void shouldThrowExceptionOnPersonalisationWhenCaseIsNull() {
        assertThatThrownBy(() -> personalisation.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void shouldReturnPersonalisationWhenAllInformationGiven() {
        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertEquals(APPEAL_REFERENCE_NUMBER_VALUE, result.get(APPEAL_REFERENCE_NUMBER_KEY));
        assertEquals(LEGAL_REP_REFERENCE_NUMBER_VALUE, result.get(LEGAL_REP_REFERENCE_NUMBER_KEY));
        assertEquals(APPELLANT_GIVEN_NAMES_VALUE, result.get(APPELLANT_GIVEN_NAMES_KEY));
        assertEquals(APPELLANT_FAMILY_NAME_VALUE, result.get(APPELLANT_FAMILY_NAME_KEY));
        assertEquals(HOME_OFFICE_REFERENCE_NUMBER_VALUE, result.get(HOME_OFFICE_REFERENCE_NUMBER_KEY));
        assertEquals(FEB_2002, result.get(DECISION_SENT_DATE_KEY));
        assertEquals(JUL_2002, result.get(WEEKS_DEADLINE_KEY));
        assertEquals(FEB_2002, result.get(APPEAL_RECEIVED_DATE_KEY));
        assertEquals(LocalDate.now().format(ofPattern(D_MMM_YYYY)), result.get(PRACTICE_DIRECTION_KEY));
        assertEquals(LocalDate.now().plusDays(DAYS_14).format(ofPattern(D_MMM_YYYY)), result.get(DAYS_14_FROM_DATE_OF_DIRECTION_KEY));
        assertEquals(LocalDate.now().plusDays(DAYS_42).format(ofPattern(D_MMM_YYYY)), result.get(DAYS_42_FROM_DATE_OF_DIRECTION_KEY));
        assertEquals(LocalDate.now().plusDays(DAYS_56).format(ofPattern(D_MMM_YYYY)), result.get(DAYS_56_FROM_DATE_OF_DIRECTION));
        assertEquals("10 Main St", result.get("address_line_1"));
    }

    @Test
    void shouldThrowExceptionWhenNoAppealReceivedDateGiven() {
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalisation.getPersonalisation(asylumCase))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Received date  is not present");
    }

    private void setupAsylumCaseMocks() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(APPEAL_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(LEGAL_REP_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of(APPELLANT_GIVEN_NAMES_VALUE));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of(APPELLANT_FAMILY_NAME_VALUE));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(HOME_OFFICE_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(COMPLETE_CASE_REVIEW_DATE, String.class))
                .thenReturn(Optional.of(REVIEW_DATE));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.of(REVIEW_DATE));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of(REVIEW_DATE));
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_HAS_ADDRESS, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(LEGAL_REP_ADDRESS_U_K, AddressUk.class))
                .thenReturn(Optional.of(new AddressUk("10 Main St", "Suite 1", "", "London", "", "EC1A 1BB", "UK")));
    }

    private void setupCustomerServicesMocks() {
        Map<String, String> customerServicesMap = new HashMap<>();
        customerServicesMap.put(CUSTOMER_SERVICES_TELEPHONE_KEY, CUSTOMER_SERVICE_PHONE);
        customerServicesMap.put(CUSTOMER_SERVICES_EMAIL_KEY, CUSTOMER_SERVICE_EMAIL);
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase))
                .thenReturn(customerServicesMap);
    }
}
