package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantRecordRefundDecisionPersonalisationEmailTest {

    private final String appellantRefundApprovedTemplateId = "appellantRefundApprovedTemplateId";
    private final String appellantRefundPartiallyApprovedTemplateId = "appellantRefundPartiallyApprovedTemplateId";
    private final String appellantRefundRejectedTemplateId = "appellantRefundRejectedTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
    private final String appellantGivenNames = "GivenNames";
    private final String appellantFamilyName = "FamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer@example.com";
    private final int daysAfterRefundDecision = 14;

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    SystemDateProvider systemDateProvider;

    private AppellantRecordRefundDecisionPersonalisationEmail appellantRecordRefundDecisionPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        String amountRemitted = "4000";
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(amountRemitted));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        appellantRecordRefundDecisionPersonalisationEmail = new AppellantRecordRefundDecisionPersonalisationEmail(
            appellantRefundApprovedTemplateId,
            appellantRefundPartiallyApprovedTemplateId,
            appellantRefundRejectedTemplateId,
            iaAipFrontendUrl,
            daysAfterRefundDecision,
            customerServicesProvider,
            recipientsFinder,
            systemDateProvider
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = RemissionDecision.class,
            names = {"APPROVED", "PARTIALLY_APPROVED", "REJECTED"})
    void should_return_approved_template_id(RemissionDecision remissionDecision) {

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));

        switch (remissionDecision) {
            case APPROVED ->
                    assertEquals(appellantRefundApprovedTemplateId, appellantRecordRefundDecisionPersonalisationEmail.getTemplateId(asylumCase));
            case PARTIALLY_APPROVED ->
                    assertEquals(appellantRefundPartiallyApprovedTemplateId, appellantRecordRefundDecisionPersonalisationEmail.getTemplateId(asylumCase));
            case REJECTED ->
                    assertEquals(appellantRefundRejectedTemplateId, appellantRecordRefundDecisionPersonalisationEmail.getTemplateId(asylumCase));
            default -> throw new IllegalArgumentException("Unexpected remission decision: " + remissionDecision);
        }
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REFUND_DECISION_DECIDED_AIP_APPELLANT_EMAIL",
                appellantRecordRefundDecisionPersonalisationEmail.getReferenceId(caseId));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        String appellantEmail = "example@example.com";
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(appellantEmail));

        assertTrue(appellantRecordRefundDecisionPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(appellantEmail));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
assertThrows(NullPointerException.class,
            () -> appellantRecordRefundDecisionPersonalisationEmail.getPersonalisation((AsylumCase) null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.APPROVED));

        final String dueDate = LocalDate.now().plusDays(daysAfterRefundDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRefundDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
                appellantRecordRefundDecisionPersonalisationEmail.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToService", iaAipFrontendUrl)
            .containsEntry("14DaysAfterRefundDecision", systemDateProvider.dueDate(daysAfterRefundDecision))
            .containsEntry("refundAmount", "40.00");

        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
