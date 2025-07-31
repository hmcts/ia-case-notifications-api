package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PrisonNomsNumber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionEngagementTeamDecideAnApplicationProductionPersonalisationTest {

    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String EMAIL = "detention@email.com";
    private static final String SUBJECT_PREFIX = "nonAdaPrefix";
    private static final String PRISON = "prison";
    private static final String OTHER = "other";

    private DetentionEngagementTeamDecideAnApplicationProductionPersonalisation personalisation;

    @Mock
    private DetentionEmailService detentionEmailService;
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
    private PrisonNomsNumber prisonNomsNumber;

    @BeforeEach
    void setUp() {
        personalisation = new DetentionEngagementTeamDecideAnApplicationProductionPersonalisation(
                TEMPLATE_ID,
                detentionEmailService,
                dateTimeExtractor,
                hearingDetailsFinder,
                SUBJECT_PREFIX
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_return_template_id() {
        assertThat(personalisation.getTemplateId()).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void should_return_reference_id() {
        assertThat(personalisation.getReferenceId(123L))
                .isEqualTo("123_DETAINED_APPLICATION_DECIDED_PRODUCTION_DET");
    }

    @Test
    void should_return_empty_recipients_if_not_in_detention() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);
        assertThat(recipients).isEmpty();
    }

    @Test
    void should_return_empty_recipients_if_detention_facility_is_other() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(OTHER));

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);
        assertThat(recipients).isEmpty();
    }

    @Test
    void should_return_recipient_if_appellant_is_detained_in_prison() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(PRISON));
        when(detentionEmailService.getDetentionEmailAddress(asylumCase)).thenReturn(EMAIL);

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);
        assertThat(recipients).containsExactly(EMAIL);
    }

    @Test
    void should_return_personalisation_with_all_data() {
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore)).thenReturn("2024-07-01T15:00");
        when(dateTimeExtractor.extractHearingDate("2024-07-01T15:00")).thenReturn("01-07-2024");
        when(dateTimeExtractor.extractHearingTime("2024-07-01T15:00")).thenReturn("15:00");
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCaseBefore)).thenReturn("hearing address");

        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(PRISON));
        when(asylumCase.read(PRISON_NOMS, PrisonNomsNumber.class)).thenReturn(Optional.of(prisonNomsNumber));
        when(prisonNomsNumber.getPrison()).thenReturn("XYZ123");

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("A123456"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO123"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Smith"));
        when(asylumCase.read(DETENTION_BUILDING, String.class)).thenReturn(Optional.of("Building A"));

        Map<String, String> personalisationMap = personalisation.getPersonalisation(callback);

        assertThat(personalisationMap)
                .containsEntry("subjectPrefix", SUBJECT_PREFIX)
                .containsEntry("appealReferenceNumber", "A123456")
                .containsEntry("homeOfficeReferenceNumber", "HO123")
                .containsEntry("appellantGivenNames", "John")
                .containsEntry("appellantFamilyName", "Smith")
                .containsEntry("nomsRef", "NOMS Ref: XYZ123")
                .containsEntry("hearingDate", "01-07-2024")
                .containsEntry("hearingTime", "15:00")
                .containsEntry("hearingCentreAddress", "hearing address")
                .containsEntry("detentionBuilding", "Building A");
    }

    @Test
    void should_return_default_values_if_optional_data_missing() {
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());

        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(PRISON));
        when(asylumCase.read(PRISON_NOMS, PrisonNomsNumber.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(DETENTION_BUILDING, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisationMap = personalisation.getPersonalisation(callback);

        assertThat(personalisationMap.get("nomsRef")).isEmpty();
        assertThat(personalisationMap.get("hearingDate")).isEmpty();
        assertThat(personalisationMap.get("hearingTime")).isEmpty();
        assertThat(personalisationMap.get("hearingCentreAddress")).isEmpty();
    }

    @Test
    void should_throw_null_pointer_when_callback_is_null() {
        Callback<AsylumCase> callback = null;
        assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation(callback));
    }
}
