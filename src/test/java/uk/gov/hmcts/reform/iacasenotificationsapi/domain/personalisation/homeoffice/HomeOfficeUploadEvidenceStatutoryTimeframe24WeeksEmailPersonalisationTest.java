package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
// HO_REFERENCE_WITH_TEXT is private in Stf24WeeksUtil; use the literal key instead of importing it
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.SUBJECT_PREFIX_KEY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.LINK_TO_ONLINE_SERVICE_KEY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeUploadEvidenceStatutoryTimeframe24WeeksEmailPersonalisationTest {

    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String IA_EX_UI_FRONTEND_URL = "http://somefrontendurl";
    private static final String NON_ADA_PREFIX = "nonAdaPrefix";
    private static final String APC_PRIVATE_HO_EMAIL = "apc.home.office@example.com";
    private static final String HEARING_DATE_TIME = "2019-08-27T14:25:15.000";
    private static final String HEARING_DATE = "2019-08-27";
    private static final String HEARING_CENTRE_ADDRESS = "some hearing centre address";
    private static final String APPEAL_REFERENCE = "A12345";
    private static final String APPELLANT_GIVEN = "John";
    private static final String APPELLANT_FAMILY = "Smith";
    private static final String HOME_OFFICE_REF = "HO-REF-123";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;

    private HomeOfficeUploadEvidenceStatutoryTimeframe24WeeksEmailPersonalisation personalisation;

    @BeforeEach
    void setup() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(APPELLANT_GIVEN));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(APPELLANT_FAMILY));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REFERENCE));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(HOME_OFFICE_REF));

        Map<String, String> customerServices = new HashMap<>();
        customerServices.put("customerServicesTelephone", "555 555 555");
        customerServices.put("customerServicesEmail", "cust.services@example.com");
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(customerServices);

        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(HEARING_DATE_TIME);
        when(dateTimeExtractor.extractHearingDate(HEARING_DATE_TIME)).thenReturn(HEARING_DATE);
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(HEARING_CENTRE_ADDRESS);

        personalisation = new HomeOfficeUploadEvidenceStatutoryTimeframe24WeeksEmailPersonalisation(
            APC_PRIVATE_HO_EMAIL,
            TEMPLATE_ID,
            IA_EX_UI_FRONTEND_URL,
            NON_ADA_PREFIX,
            customerServicesProvider,
            dateTimeExtractor,
            hearingDetailsFinder
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(TEMPLATE_ID, personalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + STATUTORY_TIMEFRAME_24WEEKS_UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE_EMAIL, personalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_configuration() {
        assertTrue(personalisation.getRecipientsList(asylumCase).contains(APC_PRIVATE_HO_EMAIL));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsEntry("appealReferenceNumber", APPEAL_REFERENCE)
            .containsEntry("appellantGivenNames", APPELLANT_GIVEN)
            .containsEntry("appellantFamilyName", APPELLANT_FAMILY)
            .containsEntry(LINK_TO_ONLINE_SERVICE_KEY, IA_EX_UI_FRONTEND_URL)
            .containsEntry("hearingDate", HEARING_DATE)
            .containsEntry("hearingCentreAddress", HEARING_CENTRE_ADDRESS)
            .containsEntry(SUBJECT_PREFIX_KEY, NON_ADA_PREFIX)
            .containsEntry("hoReferenceWithText", "Home office reference:" + HOME_OFFICE_REF);
    }

    @Test
    void should_return_empty_strings_when_optional_information_missing() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertEquals("", result.get("appellantGivenNames"));
        assertEquals("", result.get("appellantFamilyName"));
        assertEquals("", result.get("appealReferenceNumber"));
        assertEquals("Home office reference:", result.get("hoReferenceWithText"));
        assertEquals(IA_EX_UI_FRONTEND_URL, result.get(LINK_TO_ONLINE_SERVICE_KEY));
    }

    @Test
    void should_throw_null_pointer_exception_when_asylum_case_is_null() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

}

