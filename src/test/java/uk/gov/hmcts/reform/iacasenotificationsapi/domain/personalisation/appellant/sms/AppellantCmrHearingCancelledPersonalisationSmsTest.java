package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantCmrHearingCancelledPersonalisationSmsTest {

    private final Long caseId = 12345L;
    private final String cmrCancelledSmsTemplateId = "cmrCancelledSmsTemplateId";
    private final String manualCmrCancelledSmsTemplateId = "manualCmrCancelledSmsTemplateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String oldHearingCentreAddress = "someHearingCentreAddress";
    private final String oldHearingDate = "1 January 2026";
    private final String oldHearingTime = "2:00pm";
    private final String someHearingDateTime = "2026-01-01T14:00";
    private final String aipFrontendUrl = "http://somefrontendurl";
    private String appellantMobileNumber = "07781122334";

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;

    private AppellantCmrHearingCancelledPersonalisationSms appellantCmrHearingCancelledPersonalisationSms;


    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));

        when(hearingDetailsFinder.getCmrHearingDateTime(asylumCase)).thenReturn(someHearingDateTime);
        when(hearingDetailsFinder.getCmrHearingCentreLocation(asylumCase)).thenReturn(oldHearingCentreAddress);
        when(dateTimeExtractor.extractHearingDate(someHearingDateTime)).thenReturn(oldHearingDate);
        when(dateTimeExtractor.extractHearingTime(someHearingDateTime)).thenReturn(oldHearingTime);

        appellantCmrHearingCancelledPersonalisationSms = new AppellantCmrHearingCancelledPersonalisationSms(
                cmrCancelledSmsTemplateId,
                manualCmrCancelledSmsTemplateId,
                aipFrontendUrl,
                recipientsFinder,
                dateTimeExtractor,
                hearingDetailsFinder

        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(cmrCancelledSmsTemplateId, appellantCmrHearingCancelledPersonalisationSms.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_CMR_HEARING_CANCELLED_APPELLANT_AIP_SMS",
                appellantCmrHearingCancelledPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_appellant_phone_number_from_asylum_case() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(appellantMobileNumber));

        assertTrue(appellantCmrHearingCancelledPersonalisationSms.getRecipientsList(asylumCase)
                .contains(appellantMobileNumber));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
                assertThrows(NullPointerException.class, () -> appellantCmrHearingCancelledPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation = appellantCmrHearingCancelledPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
                .containsEntry("appealReferenceNumber", appealReferenceNumber)
                .containsEntry("oldHearingDate", oldHearingDate)
                .containsEntry("oldHearingTime", oldHearingTime)
                .containsEntry("oldHearingCentreAddress", oldHearingCentreAddress)
                .containsEntry("linkToService", aipFrontendUrl);
    }
}