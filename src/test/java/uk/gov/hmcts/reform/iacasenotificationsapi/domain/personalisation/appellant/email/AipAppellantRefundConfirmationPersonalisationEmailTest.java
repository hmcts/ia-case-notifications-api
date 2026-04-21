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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipAppellantRefundConfirmationPersonalisationEmailTest {

    private final String refundConfirmationInCountryTemplateId = "refundConfirmationInCountryTemplateId";
    private final String refundConfirmationOutOfCountryTemplateId = "refundConfirmationOutOfCountryTemplateId";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
    private final String appellantGivenNames = "GivenNames";
    private final String appellantFamilyName = "FamilyName";
    private final int daysAfterRemissionDecision = 14;
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";

    @Mock
    AsylumCase asylumCase;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    RecipientsFinder recipientsFinder;

    private AipAppellantRefundConfirmationPersonalisationEmail aipAppellantRefundConfirmationPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        String newFeeAmount = "8000";
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of(newFeeAmount));
        String withHearing = "decisionWithHearing";
        when(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withHearing));
        String withoutHearing = "decisionWithoutHearing";
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withoutHearing));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        String iaAipFrontendUrl = "http://localhost";
        aipAppellantRefundConfirmationPersonalisationEmail = new AipAppellantRefundConfirmationPersonalisationEmail(
            refundConfirmationInCountryTemplateId,
            refundConfirmationOutOfCountryTemplateId,
            iaAipFrontendUrl,
            daysAfterRemissionDecision,
            customerServicesProvider,
            recipientsFinder,
            systemDateProvider
        );
    }

    @Test
    void should_return_correct_template_id_with_or_without_home_office_reference() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        assertTrue(aipAppellantRefundConfirmationPersonalisationEmail.getTemplateId(asylumCase).contains(refundConfirmationInCountryTemplateId));

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        assertTrue(aipAppellantRefundConfirmationPersonalisationEmail.getTemplateId(asylumCase).contains(refundConfirmationOutOfCountryTemplateId));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REFUND_CONFIRMATION_AIP_APPELLANT_EMAIL",
            aipAppellantRefundConfirmationPersonalisationEmail.getReferenceId(12345L));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        String appellantEmail = "test@email.com";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(appellantEmail));

        assertTrue(aipAppellantRefundConfirmationPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(appellantEmail));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> aipAppellantRefundConfirmationPersonalisationEmail.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
            aipAppellantRefundConfirmationPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("previousDecisionHearingFeeOption", "Decision with hearing")
            .containsEntry("updatedDecisionHearingFeeOption", "Decision without hearing")
            .containsEntry("newFee", "80.00")
            .containsEntry("dueDate", systemDateProvider.dueDate(daysAfterRemissionDecision));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

}