package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantCmrHearingCancelledPersonalisationSmsTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCaseBefore;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    private DateTimeExtractor dateTimeExtractor;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private HearingCentre tribunalCentre = HearingCentre.HATTON_CROSS;
    private String hearingCentreAddress = "some hearing centre address";

    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";

    private String appellantGivenNames = "appellantGivenNames";
    private String appellantFamilyName = "appellantFamilyName";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantCmrHearingCancelledPersonalisationSms personalisation;

    @BeforeEach
    void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(tribunalCentre));
        personalisation = new AppellantCmrHearingCancelledPersonalisationSms(
                templateId,
                iaAipFrontendUrl,
                recipientsFinder,
                dateTimeExtractor,
                hearingDetailsFinder
        );
    }

    @Test
    public void should_return_correct_template_id() {
        assertEquals(templateId, personalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CMR_HEARING_CANCELLED_APPELLANT_SMS",
                personalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> personalisation.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case_aip() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));
        Set<String> response = personalisation.getRecipientsList(asylumCase);
        verify(recipientsFinder, times(2)).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder, times(0)).findReppedAppellant(asylumCase, NotificationType.SMS);
        assertTrue(response.contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case_rep() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(recipientsFinder.findReppedAppellant(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));
        Set<String> response = personalisation.getRecipientsList(asylumCase);
        verify(recipientsFinder, times(0)).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder, times(2)).findReppedAppellant(asylumCase, NotificationType.SMS);
        assertTrue(response.contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCaseBefore)).thenReturn("2024-06-01T10:00");
        when(dateTimeExtractor.extractHearingDate("2024-06-01T10:00")).thenReturn("01-06-2024");
        when(dateTimeExtractor.extractHearingTime("2024-06-01T10:00")).thenReturn("10:00");
        when(hearingDetailsFinder.getCmrHearingCentreAddress(asylumCaseBefore)).thenReturn("some address");

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("REF123"));

        Map<String, String> personalisation =
                this.personalisation.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);

        assertThat(personalisation)
                .containsEntry("appealReferenceNumber", "REF123")
                .containsEntry("linkToService", iaAipFrontendUrl)
                .containsEntry("oldHearingDate", "01-06-2024")
                .containsEntry("oldHearingTime", "10:00")
                .containsEntry("oldHearingCentreAddress", "some address");
    }
}