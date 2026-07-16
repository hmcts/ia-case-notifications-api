package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantCmrListingPersonalisationSmsTest {

    private final String templateId = "someTemplateId";
    private final String legallyReppedTemplateId = "legallyReppedTemplateId";
    private final String iaAipFrontendUrl = "http://somefrontendurl";
    private final String hearingCentreAddress = "some hearing centre address";
    private final String hearingDateTime = "2019-08-27T14:25:15.000";
    private final String hearingDate = "2019-08-27";
    private final String hearingTime = "14:25";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String appellantMobileNumber = "+447123456789";

    @Mock
    AsylumCase asylumCase;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    @Mock
    RecipientsFinder recipientsFinder;

    private AppellantCmrListingPersonalisationSms appellantCmrListingPersonalisationSms;

    @BeforeEach
    void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));

        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);
        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);

        appellantCmrListingPersonalisationSms = new AppellantCmrListingPersonalisationSms(
            templateId,
            legallyReppedTemplateId,
            iaAipFrontendUrl,
            dateTimeExtractor,
            hearingDetailsFinder,
            recipientsFinder
        );
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_CMR_LISTING_APPELLANT_SMS",
            appellantCmrListingPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_correct_template_id() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertEquals(templateId, appellantCmrListingPersonalisationSms.getTemplateId(asylumCase));

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertEquals(legallyReppedTemplateId, appellantCmrListingPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_mobile_number_from_subscribers_when_aip() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantMobileNumber));

        assertTrue(appellantCmrListingPersonalisationSms.getRecipientsList(asylumCase)
            .contains(appellantMobileNumber));
        verify(recipientsFinder, times(1)).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder, times(0)).findReppedAppellant(asylumCase, NotificationType.SMS);
    }

    @Test
    void should_return_given_mobile_number_from_case_when_repped() {
        when(recipientsFinder.findReppedAppellant(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantMobileNumber));

        assertTrue(appellantCmrListingPersonalisationSms.getRecipientsList(asylumCase)
            .contains(appellantMobileNumber));
        verify(recipientsFinder, times(0)).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder, times(1)).findReppedAppellant(asylumCase, NotificationType.SMS);
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantCmrListingPersonalisationSms.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantCmrListingPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation = appellantCmrListingPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl)
            .containsEntry("hearingDate", hearingDate)
            .containsEntry("hearingTime", hearingTime)
            .containsEntry("hearingCentreAddress", hearingCentreAddress);
    }

    @Test
    void should_return_personalisation_when_mandatory_information_missing() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = appellantCmrListingPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation).containsEntry("appealReferenceNumber", "");
    }
}
