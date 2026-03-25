package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeCmrHearingCancelledPersonalisationTest {

    private static final String TEMPLATE_ID = "templateId";
    private static final String LINK_TO_ONLINE_SERVICE = "link";
    private static final String EMAIL = "detention@email.com";

    private HomeOfficeCmrHearingCancelledPersonalisation personalisation;

    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCaseBefore;
    @Mock
    private EmailAddressFinder emailAddressFinder;

    @BeforeEach
    void setUp() {
        personalisation = new HomeOfficeCmrHearingCancelledPersonalisation(
                TEMPLATE_ID, LINK_TO_ONLINE_SERVICE, dateTimeExtractor, hearingDetailsFinder, emailAddressFinder);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)).thenReturn(EMAIL);
    }

    @Test
    void should_return_template_id() {
        assertThat(personalisation.getTemplateId()).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void should_return_reference_id() {
        assertThat(personalisation.getReferenceId(12345L))
                .isEqualTo("12345_CMR_HEARING_CANCELLED_HOME_OFFICE");
    }

    @Test
    void should_return_recipients_if_detained_in_prison() {
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(emailAddressFinder.getCmrHearingCancelledCaseOfficerHearingCentreEmailAddress(asylumCase)).thenReturn(EMAIL);

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);
        assertThat(recipients).containsExactly(EMAIL);
    }

    @Test
    void should_return_personalisation_with_values() {
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCaseBefore)).thenReturn("2024-06-01T10:00");
        when(dateTimeExtractor.extractHearingDate("2024-06-01T10:00")).thenReturn("01-06-2024");
        when(dateTimeExtractor.extractHearingTime("2024-06-01T10:00")).thenReturn("10:00");
        when(hearingDetailsFinder.getCmrHearingCentreAddress(asylumCaseBefore)).thenReturn("some address");

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("REF123"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("REF456"));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO123"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));

        Map<String, String> personalisationMap = personalisation.getPersonalisation(callback);

        assertThat(personalisationMap)
                .containsEntry("appealReferenceNumber", "REF123")
                .containsEntry("homeOfficeReferenceNumber", "REF456")
                .containsEntry("appellantGivenNames", "John")
                .containsEntry("appellantFamilyName", "Doe")
                .containsEntry("linkToOnlineService", LINK_TO_ONLINE_SERVICE)
                .containsEntry("oldHearingDate", "01-06-2024")
                .containsEntry("oldHearingTime", "10:00")
                .containsEntry("oldHearingCentreAddress", "some address");
    }

    @Test
    @Disabled
    void should_return_empty_hearing_info_if_no_previous_case_details() {
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisationMap = personalisation.getPersonalisation(callback);

        assertThat(personalisationMap.get("oldHearingDate")).isEmpty();
        assertThat(personalisationMap.get("oldHearingTime")).isEmpty();
        assertThat(personalisationMap.get("oldHearingCentreAddress")).isEmpty();
    }

    @Test
    void should_throw_exception_on_null_callback() {
        Callback<AsylumCase> callback = null;
        assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation(callback));
    }
}
