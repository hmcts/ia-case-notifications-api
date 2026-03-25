package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
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
public class AppellantCmrListingPersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;

    private Long caseId = 12345L;
    private String legallyReppedTemplateId = "legallyReppedTemplateId";
    private String aipTemplateId = "aipTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";
    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";
    private String hearingCentreAddress = "some hearing centre address";
    private String hearingCentreName = "Taylor House";

    private AppellantCmrListingPersonalisationSms appellantCmrListingPersonalisationSms;

    @BeforeEach
    void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(hearingCentreName);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        appellantCmrListingPersonalisationSms = new AppellantCmrListingPersonalisationSms(
                legallyReppedTemplateId,
                aipTemplateId,
                iaExUiFrontendUrl,
                recipientsFinder,
                dateTimeExtractor,
                personalisationProvider,
                hearingDetailsFinder
        );
    }

    @Test
    public void should_return_legally_repped_template_id_when_legal_rep_present() {
        when(asylumCase.read(LEGAL_REP_NAME)).thenReturn(Optional.of("Some Legal Rep"));
        assertEquals(legallyReppedTemplateId, appellantCmrListingPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_aip_template_id_when_legal_rep_absent() {
        when(asylumCase.read(LEGAL_REP_NAME)).thenReturn(Optional.empty());
        assertEquals(aipTemplateId, appellantCmrListingPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LR_CMR_LISTED_APPELLANT_SMS",
                appellantCmrListingPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {
        when(recipientsFinder.findAll(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantCmrListingPersonalisationSms.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_list_from_subscribers_in_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        Set<String> response = appellantCmrListingPersonalisationSms.getRecipientsList(asylumCase);

        verify(recipientsFinder, times(1)).findAll(asylumCase, NotificationType.SMS);
        assertTrue(response.contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
                appellantCmrListingPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation).isNotEmpty();
        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(hearingCentreName, personalisation.get("tribunalCentre"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("hyperlink to service"));
    }

    @Test
    public void should_return_personalisation_when_optional_fields_are_blank() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn("");
        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn("");
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn("");
        when(dateTimeExtractor.extractHearingDate("")).thenReturn("");
        when(dateTimeExtractor.extractHearingTime("")).thenReturn("");

        Map<String, String> personalisation =
                appellantCmrListingPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation).isNotEmpty();
        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("hearingDate"));
        assertEquals("", personalisation.get("hearingTime"));
        assertEquals("", personalisation.get("hearingCentreAddress"));
        assertEquals("", personalisation.get("tribunalCentre"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("hyperlink to service"));
    }
}