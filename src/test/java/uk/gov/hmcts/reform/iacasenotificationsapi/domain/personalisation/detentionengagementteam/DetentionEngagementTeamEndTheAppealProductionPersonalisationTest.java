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
class DetentionEngagementTeamEndTheAppealProductionPersonalisationTest {

    private static final String TEMPLATE_ID = "end-appeal-template-id";
    private static final String EMAIL = "detained@email.com";
    private static final String SUBJECT_PREFIX = "Appeal Ended:";
    private static final String PRISON = "prison";
    private static final String OTHER = "other";

    private DetentionEngagementTeamEndTheAppealProductionPersonalisation personalisation;

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
        personalisation = new DetentionEngagementTeamEndTheAppealProductionPersonalisation(
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
                .isEqualTo("123_DETAINED_CASE_LISTED_PRODUCTION_DET");
    }

    @Test
    void should_return_empty_recipients_if_not_detained() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.empty());

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);
        assertThat(recipients).isEmpty();
    }

    @Test
    void should_return_empty_recipients_if_facility_is_other() {
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(OTHER));

        Set<String> recipients = personalisation.getRecipientsList(asylumCase);
        assertThat(recipients).isEmpty();
    }

    @Test
    void should_return_detention_email_if_in_prison() {
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

        when(hearingDetailsFinder.getHearingDateTime(asylumCaseBefore)).thenReturn("2025-07-31T12:00");
        when(dateTimeExtractor.extractHearingDate("2025-07-31T12:00")).thenReturn("31-07-2025");
        when(dateTimeExtractor.extractHearingTime("2025-07-31T12:00")).thenReturn("12:00");
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCaseBefore)).thenReturn("Hearing Address");

        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(PRISON));
        when(asylumCase.read(PRISON_NOMS, PrisonNomsNumber.class)).thenReturn(Optional.of(prisonNomsNumber));
        when(prisonNomsNumber.getPrison()).thenReturn("PR123");

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("REF001"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO001"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Jane"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));
        when(asylumCase.read(DETENTION_BUILDING, String.class)).thenReturn(Optional.of("Building X"));

        Map<String, String> result = personalisation.getPersonalisation(callback);

        assertThat(result)
                .containsEntry("subjectPrefix", SUBJECT_PREFIX)
                .containsEntry("appealReferenceNumber", "REF001")
                .containsEntry("homeOfficeReferenceNumber", "HO001")
                .containsEntry("appellantGivenNames", "Jane")
                .containsEntry("appellantFamilyName", "Doe")
                .containsEntry("nomsRef", "NOMS Ref: PR123")
                .containsEntry("hearingDate", "31-07-2025")
                .containsEntry("hearingTime", "12:00")
                .containsEntry("hearingCentreAddress", "Hearing Address")
                .containsEntry("detentionBuilding", "Building X");
    }

    @Test
    void should_return_empty_defaults_if_previous_data_is_missing() {
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());

        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(PRISON));
        when(asylumCase.read(PRISON_NOMS, PrisonNomsNumber.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(DETENTION_BUILDING, String.class)).thenReturn(Optional.empty());

        Map<String, String> result = personalisation.getPersonalisation(callback);

        assertThat(result.get("nomsRef")).isEmpty();
        assertThat(result.get("hearingDate")).isEmpty();
        assertThat(result.get("hearingTime")).isEmpty();
        assertThat(result.get("hearingCentreAddress")).isEmpty();
    }

    @Test
    void should_throw_exception_when_callback_is_null() {
        Callback<AsylumCase> callback = null;
        assertThrows(NullPointerException.class, () -> personalisation.getPersonalisation(callback));
    }
}
