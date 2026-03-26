package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantCmrRelistingPersonalisationSmsTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String inPersonTemplateId = "inPersonTemplateId";
    private String remoteTemplateId = "remoteTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String hearingCentreAddress = "some hearing centre address";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";
    private String appellantGivenNames = "appellantGivenNames";
    private String appellantFamilyName = "appellantFamilyName";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private AppellantCmrRelistingPersonalisationSms appellantCmrRelistingPersonalisationSms;

    @BeforeEach
    void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantCmrRelistingPersonalisationSms = new AppellantCmrRelistingPersonalisationSms(
                inPersonTemplateId,
                iaExUiFrontendUrl,
                recipientsFinder,
                personalisationProvider
        );
    }

    @Test
    public void should_return_correct_template_id() {
        assertEquals(inPersonTemplateId, appellantCmrRelistingPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CMR_RELISTED_APPELLANT_SMS",
                appellantCmrRelistingPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {
        when(recipientsFinder.findAll(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantCmrRelistingPersonalisationSms.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_list_from_subscribers_in_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        Set<String> response = appellantCmrRelistingPersonalisationSms.getRecipientsList(asylumCase);

        verify(recipientsFinder, times(1)).findAll(asylumCase, NotificationType.SMS);
        assertTrue(response.contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithGivenValues());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, String> personalisation =
                appellantCmrRelistingPersonalisationSms.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("hyperlink to service"));
    }

    @Test
    public void should_return_personalisation_when_optional_fields_are_blank() {
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationMapWithBlankValues());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, String> personalisation =
                appellantCmrRelistingPersonalisationSms.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("hearingDate"));
        assertEquals("", personalisation.get("hearingTime"));
        assertEquals("", personalisation.get("hearingCentreAddress"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("hyperlink to service"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", mockedAppealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("hearingDate", hearingDate)
                .put("hearingTime", hearingTime)
                .put("hearingCentreAddress", hearingCentreAddress)
                .build();
    }

    private Map<String, String> getPersonalisationMapWithBlankValues() {
        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", "")
                .put("appellantGivenNames", "")
                .put("appellantFamilyName", "")
                .put("hearingDate", "")
                .put("hearingTime", "")
                .put("hearingCentreAddress", "")
                .build();
    }
}