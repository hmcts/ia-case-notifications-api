package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MOBILE_NUMBER;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantNonStandardDirectionPersonalisationSmsTest {

    private final Long caseId = 12345L;
    private final String smsTemplateId = "someSmsTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppellantMobilePhone = "07123456789";
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    private AppellantNonStandardDirectionPersonalisationSms appellantNonStandardDirectionPersonalisationSms;

    @BeforeEach
    void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppellantMobilePhone));

        appellantNonStandardDirectionPersonalisationSms = new AppellantNonStandardDirectionPersonalisationSms(
            smsTemplateId,
            iaAipFrontendUrl,
            recipientsFinder);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantNonStandardDirectionPersonalisationSms.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPELLANT_NON_STANDARD_DIRECTION_SMS",
            appellantNonStandardDirectionPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantNonStandardDirectionPersonalisationSms.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(
            appellantNonStandardDirectionPersonalisationSms.getRecipientsList(asylumCase).contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_callback_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class, () -> appellantNonStandardDirectionPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = appellantNonStandardDirectionPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", mockedAppealReferenceNumber)
            .containsEntry("linkToTimelinePage", iaAipFrontendUrl);
    }
}
