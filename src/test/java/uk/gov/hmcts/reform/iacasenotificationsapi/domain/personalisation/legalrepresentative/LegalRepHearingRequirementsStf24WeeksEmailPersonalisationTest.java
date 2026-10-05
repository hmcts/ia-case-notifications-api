package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ADDITIONAL_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IN_CAMERA_COURT_DECISION_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IN_CAMERA_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MULTIMEDIA_DECISION_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MULTIMEDIA_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SINGLE_SEX_COURT_DECISION_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SINGLE_SEX_COURT_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.VULNERABILITIES_DECISION_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.VULNERABILITIES_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.Stf24WeeksUtil.STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_EMAIL;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepHearingRequirementsStf24WeeksEmailPersonalisationTest {

    private static final String TEMPLATE_ID = "email-template-123";
    private static final String IA_EX_UI_FRONTEND_URL = "http://localhost/appeal";
    private static final String NON_ADA_PREFIX = "Immigration and Asylum appeal";
    private static final String LEGAL_REP_EMAIL = "legalrep@example.com";
    private static final Long CASE_ID = 12345L;
    private static final String APPELLANT_GIVEN_NAMES_VALUE = "Jane";
    private static final String APPELLANT_FAMILY_NAME_VALUE = "Smith";
    private static final String APPEAL_REFERENCE_NUMBER_VALUE = "PA/12345/2024";
    private static final String LEGAL_REP_REFERENCE_NUMBER_VALUE = "LR-REF-123";
    private static final String HEARING_DATE_TIME = "2024-09-23T09:30:00";
    private static final String HEARING_DATE = "23 Sep 2024";
    private static final String HEARING_CENTRE_ADDRESS = "Taylor House";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;

    private LegalRepHearingRequirementsStf24WeeksEmailPersonalisation personalisation;

    @BeforeEach
    void setUp() {
        when(customerServicesProvider.getCustomerServicesPersonalisation(asylumCase)).thenReturn(Map.of(
            "customerServicesTelephone", "0300 123 1711",
            "customerServicesEmail", "customer@example.com"
        ));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(APPELLANT_GIVEN_NAMES_VALUE));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(APPELLANT_FAMILY_NAME_VALUE));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(APPEAL_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(LEGAL_REP_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(LEGAL_REP_EMAIL));
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(VULNERABILITIES_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(MULTIMEDIA_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IN_CAMERA_COURT_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("some vulnerabilities"));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("some multimedia"));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("some single sex response"));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("some in camera response"));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("some other adjustments"));
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(HEARING_DATE_TIME);
        when(dateTimeExtractor.extractHearingDate(HEARING_DATE_TIME)).thenReturn(HEARING_DATE);
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(HEARING_CENTRE_ADDRESS);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

        personalisation = new LegalRepHearingRequirementsStf24WeeksEmailPersonalisation(
            TEMPLATE_ID,
            IA_EX_UI_FRONTEND_URL,
            NON_ADA_PREFIX,
            customerServicesProvider,
            dateTimeExtractor,
            hearingDetailsFinder
        );
    }

    @Test
    void shouldReturnConfiguredTemplateId() {
        assertEquals(TEMPLATE_ID, personalisation.getTemplateId());
    }

    @Test
    void shouldReturnLegalRepresentativeEmailAsRecipient() {
        Set<String> recipients = personalisation.getRecipientsList(asylumCase);

        assertEquals(Set.of(LEGAL_REP_EMAIL), recipients);
    }

    @Test
    void shouldReturnReferenceIdUsingCaseId() {
        assertEquals(CASE_ID + STATUTORY_TIMEFRAME_24WEEKS_SUBMITTED_HEARING_REQUIREMENTS_LR_EMAIL,
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
            .containsEntry("customerServicesTelephone", "0300 123 1711")
            .containsEntry("customerServicesEmail", "customer@example.com")
            .containsEntry("lrReferenceWithText", "Your reference:" + LEGAL_REP_REFERENCE_NUMBER_VALUE)
            .containsEntry("hearingRequirementVulnerabilities", "some vulnerabilities")
            .containsEntry("hearingRequirementMultimedia", "some multimedia")
            .containsEntry("hearingRequirementSingleSexCourt", "some single sex response")
            .containsEntry("hearingRequirementInCameraCourt", "some in camera response")
            .containsEntry("hearingRequirementOther", "some other adjustments");
    }

    @Test
    void shouldReturnEmptyValuesWhenOptionalPersonalisationDataIsMissing() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertThat(result)
            .containsEntry("subjectPrefix", NON_ADA_PREFIX)
            .containsEntry("linkToOnlineService", IA_EX_UI_FRONTEND_URL)
            .containsEntry("appellantGivenNames", "")
            .containsEntry("appellantFamilyName", "")
            .containsEntry("appealReferenceNumber", "")
            .containsEntry("lrReferenceWithText", "Your reference:");
    }

    @Test
    void shouldReturnEmptyRecipientListForAipJourney() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        assertTrue(personalisation.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenAsylumCaseIsNull() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation((AsylumCase) null));

        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
