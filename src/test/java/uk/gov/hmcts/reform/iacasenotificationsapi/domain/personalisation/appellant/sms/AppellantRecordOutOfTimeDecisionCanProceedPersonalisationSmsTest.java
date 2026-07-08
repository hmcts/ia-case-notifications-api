package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;


@ExtendWith(MockitoExtension.class)
class AppellantRecordOutOfTimeDecisionCanProceedPersonalisationSmsTest {


    private final String iaAipFrontendUrl = "http://localhost";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private AppellantRecordOutOfTimeDecisionCanProceedPersonalisationSms
        recordOutOfTimeDecisionCanProceedPersonalisationSms;

    @BeforeEach
    void setUp() {

        String recordOutOfDecisionCanProceedTemplateId = "recordOutOfDecisionCanProceedTemplateId";
        recordOutOfTimeDecisionCanProceedPersonalisationSms =
            new AppellantRecordOutOfTimeDecisionCanProceedPersonalisationSms(
                recordOutOfDecisionCanProceedTemplateId,
                iaAipFrontendUrl, recipientsFinder);

    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        String appealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));

        Map<String, String> personalisation =
            recordOutOfTimeDecisionCanProceedPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", appealReferenceNumber)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);

    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_RECORD_OUT_OF_TIME_DECISION_CAN_PROCEED_AIP_SMS",
            recordOutOfTimeDecisionCanProceedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        String mockedAppellantMobileNumber = "1234445556";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobileNumber));

        assertTrue(recordOutOfTimeDecisionCanProceedPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobileNumber));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> recordOutOfTimeDecisionCanProceedPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
