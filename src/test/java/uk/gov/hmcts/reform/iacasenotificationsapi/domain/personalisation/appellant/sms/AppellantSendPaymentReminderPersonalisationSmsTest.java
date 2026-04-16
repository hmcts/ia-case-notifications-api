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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantSendPaymentReminderPersonalisationSmsTest {

    private final int daysAfterNotification = 7;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String templateId = "templateId";
    private final String appellantMobileNumber = "07781122334";
    private final String ccdReferenceNumber = "1111 2222 3333 4444";
    @Mock
    AsylumCase asylumCase;
    @Mock
    SystemDateProvider systemDateProvider;
    private AppellantSendPaymentReminderPersonalisationSms appellantSendPaymentReminderPersonalisationSms;

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(INTERNAL_APPELLANT_MOBILE_NUMBER, String.class)).thenReturn(Optional.ofNullable(appellantMobileNumber));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumber));
        String feeAmount = "14000";
        when(asylumCase.read(AsylumCaseDefinition.FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));

        appellantSendPaymentReminderPersonalisationSms = new AppellantSendPaymentReminderPersonalisationSms(
            templateId,
            daysAfterNotification,
            systemDateProvider
        );
    }

    @Test
    public void should_return_given_email() {
        assertEquals(Collections.singleton(appellantMobileNumber),
            appellantSendPaymentReminderPersonalisationSms.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId,
            appellantSendPaymentReminderPersonalisationSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_INTERNAL_PAYMENT_REMINDER_APPELLANT_SMS",
            appellantSendPaymentReminderPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given_in_country() {
        final String dueDate = LocalDate.now().plusDays(daysAfterNotification)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterNotification)).thenReturn(dueDate);

        Map<String, String> personalisation =
            appellantSendPaymentReminderPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("onlineCaseReferenceNumber", ccdReferenceNumber)
            .containsEntry("feeAmount", "140.00")
            .containsEntry("dueDate", dueDate);
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantSendPaymentReminderPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
