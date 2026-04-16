package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantSendPaymentReminderPersonalisationEmailTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    SystemDateProvider systemDateProvider;
    private AppellantSendPaymentReminderPersonalisationEmail appellantSendPaymentReminderPersonalisationEmail;
    private final int daysAfterNotification = 7;
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String templateId = "templateId";
    private final String customerServicesPhone = "0100000000";
    private final String customerServicesEmail = "services@email.com";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String appellantEmail = "test@test.com";
    private final String ccdReferenceNumber = "1111 2222 3333 4444";
    private final Map<String, String> customerServices = Map.of("customerServicesTelephone", customerServicesPhone,
        "customerServicesEmail", customerServicesEmail);

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(customerServices);
        when(asylumCase.read(INTERNAL_APPELLANT_EMAIL, String.class)).thenReturn(Optional.ofNullable(appellantEmail));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumber));
        String feeAmount = "14000";
        when(asylumCase.read(AsylumCaseDefinition.FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));

        appellantSendPaymentReminderPersonalisationEmail = new AppellantSendPaymentReminderPersonalisationEmail(
            templateId,
            daysAfterNotification,
            systemDateProvider,
            customerServicesProvider
        );
    }

    @Test
    public void should_return_given_email() {
        assertEquals(Collections.singleton(appellantEmail),
            appellantSendPaymentReminderPersonalisationEmail.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId,
            appellantSendPaymentReminderPersonalisationEmail.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_INTERNAL_PAYMENT_REMINDER_APPELLANT_EMAIL",
            appellantSendPaymentReminderPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(daysAfterNotification)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterNotification)).thenReturn(dueDate);

        Map<String, String> personalisation =
            appellantSendPaymentReminderPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .containsEntry("customerServicesTelephone", customerServicesPhone)
            .containsEntry("customerServicesEmail", customerServicesEmail)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("onlineCaseReferenceNumber", ccdReferenceNumber)
            .containsEntry("feeAmount", "140.00")
            .containsEntry("dueDate", dueDate);
    }

    @Test
    public void should_throw_exception_when_callback_is_null() {

        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> appellantSendPaymentReminderPersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }
}
