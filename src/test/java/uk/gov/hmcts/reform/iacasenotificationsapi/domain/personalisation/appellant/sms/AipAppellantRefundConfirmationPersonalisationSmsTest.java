package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipAppellantRefundConfirmationPersonalisationSmsTest {

    private final String refundConfirmationTemplateId = "refundConfirmationTemplateId";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final int daysAfterRemissionDecision = 14;

    @Mock
    AsylumCase asylumCase;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    RecipientsFinder recipientsFinder;

    private AipAppellantRefundConfirmationPersonalisationSms aipAppellantRefundConfirmationPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        String newFeeAmount = "8000";
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of(newFeeAmount));
        String withHearing = "decisionWithHearing";
        when(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withHearing));
        String withoutHearing = "decisionWithoutHearing";
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withoutHearing));

        String iaAipFrontendUrl = "http://localhost";
        aipAppellantRefundConfirmationPersonalisationSms = new AipAppellantRefundConfirmationPersonalisationSms(
            refundConfirmationTemplateId,
            iaAipFrontendUrl,
            daysAfterRemissionDecision,
            recipientsFinder,
            systemDateProvider
        );
    }

    @Test
    void should_return_approved_template_id() {
        assertTrue(aipAppellantRefundConfirmationPersonalisationSms.getTemplateId(asylumCase).contains(refundConfirmationTemplateId));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REFUND_CONFIRMATION_AIP_APPELLANT_SMS",
            aipAppellantRefundConfirmationPersonalisationSms.getReferenceId(12345L));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        String appellantMobileNumber = "07781122334";
        Mockito.when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantMobileNumber));

        assertTrue(aipAppellantRefundConfirmationPersonalisationSms.getRecipientsList(asylumCase)
            .contains(appellantMobileNumber));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> aipAppellantRefundConfirmationPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
            aipAppellantRefundConfirmationPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("previousDecisionHearingFeeOption", "Decision with hearing")
            .containsEntry("updatedDecisionHearingFeeOption", "Decision without hearing")
            .containsEntry("newFee", "80.00")
            .containsEntry("dueDate", systemDateProvider.dueDate(daysAfterRemissionDecision));
    }

}
