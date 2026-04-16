package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantChangeHearingCentrePersonalisationSmsTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    AsylumCase asylumCase;
    @Mock
    AsylumCase asylumCaseBefore;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    StringProvider stringProvider;

    private final String smsTemplateId = "someSmsTemplateId";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private final String hearingCentreName = "Taylor House";
    private final HearingCentre oldHearingCentre = HearingCentre.MANCHESTER;
    private final String oldHearingCentreName = "Manchester";

    private final String mockedAppealReferenceNumber = "someReferenceNumber";

    private AppellantChangeHearingCentrePersonalisationSms appellantChangeHearingCentrePersonalisationSms;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCaseBefore.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(oldHearingCentre));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(stringProvider.get("hearingCentreName", oldHearingCentre.toString())).thenReturn(Optional.of(oldHearingCentreName));
        when(stringProvider.get("hearingCentreName", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreName));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));
        String removeAppealReason = "some remove appeal reason";
        when(asylumCase.read(REMOVE_APPEAL_FROM_ONLINE_REASON, String.class))
                .thenReturn(Optional.of(removeAppealReason));

        appellantChangeHearingCentrePersonalisationSms = new AppellantChangeHearingCentrePersonalisationSms(
                smsTemplateId,
                recipientsFinder,
                stringProvider);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantChangeHearingCentrePersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_CHANGE_HEARING_CENTRE_AIP_APPELLANT_SMS",
                appellantChangeHearingCentrePersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
assertThrows(NullPointerException.class, () -> appellantChangeHearingCentrePersonalisationSms.getRecipientsList(null))
                ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        String mockedAppellantMobilePhone = "07123456789";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantChangeHearingCentrePersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
assertThrows(NullPointerException.class,
                () -> appellantChangeHearingCentrePersonalisationSms.getPersonalisation((Callback<AsylumCase>) null))
                ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
                appellantChangeHearingCentrePersonalisationSms.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("oldHearingCentre", oldHearingCentreName)
            .containsEntry("newHearingCentre", hearingCentreName);
    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMOVE_APPEAL_FROM_ONLINE_REASON, String.class))
                .thenReturn(Optional.empty());

        Map<String, String> personalisation =
                appellantChangeHearingCentrePersonalisationSms.getPersonalisation(callback);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("oldHearingCentre", oldHearingCentreName)
            .containsEntry("newHearingCentre", hearingCentreName);

    }
}
