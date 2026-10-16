package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IN_CAMERA_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MULTIMEDIA_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SINGLE_SEX_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.VULNERABILITIES_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_APPELLANT_EMAIL;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantSubmittedHearingRequirementsStf24WeeksEmailPersonalisationTest {

    private static final String TEMPLATE_ID = "templateId";
    private static final String IA_EX_UI_FRONTEND_URL = "http://localhost/appeal";
    private static final String NON_ADA_PREFIX = "Immigration and Asylum appeal";
    private static final String APPELLANT_GIVEN_NAMES_VALUE = "someAppellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_VALUE = "someAppellantFamilyName";
    private static final String APPEAL_REFERENCE_NUMBER_VALUE = "someReferenceNumber";
    private static final String HOME_OFFICE_REFERENCE_NUMBER_VALUE = "someHomeOfficeRefNumber";
    private static final String HEARING_DATE_TIME = "2024-09-23T09:30:00";
    private static final String HEARING_DATE = "23 Sep 2024";
    private static final String HEARING_CENTRE_ADDRESS = "Taylor House";
    private static final String APPRENT_EMAIL = "appellant@example.com";
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private RecipientsFinder recipientsFinder;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;

    private AppellantSubmittedHearingRequirementsStf24WeeksEmailPersonalisation personalisation;

    @BeforeEach
    void setUp() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL)).thenReturn(Collections.singleton(APPRENT_EMAIL));
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(Map.of());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(APPELLANT_GIVEN_NAMES_VALUE));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(APPELLANT_FAMILY_NAME_VALUE));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(HOME_OFFICE_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("some vulnerabilities"));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("some multimedia"));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("some single sex court response"));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("some in camera response"));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of("some other adjustments"));
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(HEARING_DATE_TIME);
        when(dateTimeExtractor.extractHearingDate(HEARING_DATE_TIME)).thenReturn(HEARING_DATE);
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(HEARING_CENTRE_ADDRESS);

        personalisation = new AppellantSubmittedHearingRequirementsStf24WeeksEmailPersonalisation(
            TEMPLATE_ID,
            IA_EX_UI_FRONTEND_URL,
            NON_ADA_PREFIX,
            recipientsFinder,
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

        assertEquals(caseId + STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_APPELLANT_EMAIL,
            personalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_addresses_from_recipients_finder() {
        assertTrue(personalisation.getRecipientsList(asylumCase).contains(APPRENT_EMAIL));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsEntry("subjectPrefix", NON_ADA_PREFIX)
            .containsEntry("linkToOnlineService", IA_EX_UI_FRONTEND_URL)
            .containsEntry("appellantGivenNames", APPELLANT_GIVEN_NAMES_VALUE)
            .containsEntry("appellantFamilyName", APPELLANT_FAMILY_NAME_VALUE)
            .containsEntry("appealReferenceNumber", APPEAL_REFERENCE_NUMBER_VALUE)
            .containsEntry("hearingDate", HEARING_DATE)
            .containsEntry("hearingCentreAddress", HEARING_CENTRE_ADDRESS)
            .containsEntry("hearingRequirementVulnerabilities", "some vulnerabilities")
            .containsEntry("hearingRequirementMultimedia", "some multimedia")
            .containsEntry("hearingRequirementSingleSexCourt", "some single sex court response")
            .containsEntry("hearingRequirementInCameraCourt", "some in camera response")
            .containsEntry("hearingRequirementOther", "some other adjustments");
    }

    @Test
    void should_return_personalisation_when_mandatory_information_is_missing() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsEntry("subjectPrefix", NON_ADA_PREFIX)
            .containsEntry("linkToOnlineService", IA_EX_UI_FRONTEND_URL)
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("hearingRequirementVulnerabilities", "some vulnerabilities")
            .containsEntry("hearingRequirementMultimedia", "some multimedia")
            .containsEntry("hearingRequirementSingleSexCourt", "some single sex court response")
            .containsEntry("hearingRequirementInCameraCourt", "some in camera response")
            .containsEntry("hearingRequirementOther", "some other adjustments");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation((AsylumCase) null));

        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
