package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

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

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_HOME_OFFICE_EMAIL;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeHearingRequirementsStf24WeeksEmailPersonalisationTest {

    private static final String TEMPLATE_ID = "templateId";
    private static final String IA_EX_UI_FRONTEND_URL = "http://localhost/appeal";
    private static final String NON_ADA_PREFIX = "Immigration and Asylum appeal";
    private static final String HOME_OFFICE_EMAIL = "homeoffice@example.com";
    private static final Long CASE_ID = 12345L;
    private static final String APPELLANT_GIVEN_NAMES_VALUE = "SomeAppellant";
    private static final String APPELLANT_FAMILY_NAME_VALUE = "Surname";
    private static final String APPEAL_REFERENCE_NUMBER_VALUE = "AP-2024-123";
    private static final String HOME_OFFICE_REFERENCE_NUMBER_VALUE = "HO-12345";
    private static final String HEARING_DATE_TIME = "2024-10-15T14:30:00";
    private static final String HEARING_DATE = "15 Oct 2024";
    private static final String HEARING_CENTRE_ADDRESS = "Manchester Tribunal Hearing Centre";

    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;
    @Mock
    private AsylumCase asylumCase;

    private HomeOfficeHearingRequirementsStf24WeeksEmailPersonalisation personalisation;

    @BeforeEach
    void setUp() {
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(Map.of());
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(HEARING_DATE_TIME);
        when(dateTimeExtractor.extractHearingDate(HEARING_DATE_TIME)).thenReturn(HEARING_DATE);
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(HEARING_CENTRE_ADDRESS);
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(APPELLANT_GIVEN_NAMES_VALUE));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(APPELLANT_FAMILY_NAME_VALUE));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(HOME_OFFICE_REFERENCE_NUMBER_VALUE));

        personalisation = new HomeOfficeHearingRequirementsStf24WeeksEmailPersonalisation(
            HOME_OFFICE_EMAIL,
            TEMPLATE_ID,
            IA_EX_UI_FRONTEND_URL,
            NON_ADA_PREFIX,
            customerServicesProvider,
            dateTimeExtractor,
            hearingDetailsFinder
        );
    }

    @Test
    void shouldReturnTheConfiguredTemplateId() {
        assertEquals(TEMPLATE_ID, personalisation.getTemplateId());
    }

    @Test
    void shouldReturnTheConfiguredHomeOfficeEmailAddress() {
        assertTrue(personalisation.getRecipientsList(asylumCase).contains(HOME_OFFICE_EMAIL));
    }

    @Test
    void shouldReturnTheConfiguredReferenceId() {
        assertEquals(CASE_ID + STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_HOME_OFFICE_EMAIL,
            personalisation.getReferenceId(CASE_ID));
    }

    @Test
    void shouldReturnPersonalisationWhenAllRequiredInformationIsAvailable() {
        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsEntry("subjectPrefix", NON_ADA_PREFIX)
            .containsEntry("linkToOnlineService", IA_EX_UI_FRONTEND_URL)
            .containsEntry("appellantGivenNames", APPELLANT_GIVEN_NAMES_VALUE)
            .containsEntry("appellantFamilyName", APPELLANT_FAMILY_NAME_VALUE)
            .containsEntry("appealReferenceNumber", APPEAL_REFERENCE_NUMBER_VALUE)
            .containsEntry("hearingDate", HEARING_DATE)
            .containsEntry("hearingCentreAddress", HEARING_CENTRE_ADDRESS)
            .containsEntry("hoReferenceWithText", "Home office reference:" + HOME_OFFICE_REFERENCE_NUMBER_VALUE);
    }

    @Test
    void shouldReturnEmptyValuesWhenOptionalPersonalisationDataIsMissing() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsEntry("subjectPrefix", NON_ADA_PREFIX)
            .containsEntry("linkToOnlineService", IA_EX_UI_FRONTEND_URL)
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("hoReferenceWithText", "Home office reference:");
    }

    @Test
    void shouldThrowExceptionWhenAsylumCaseIsNull() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation((AsylumCase) null));

        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
