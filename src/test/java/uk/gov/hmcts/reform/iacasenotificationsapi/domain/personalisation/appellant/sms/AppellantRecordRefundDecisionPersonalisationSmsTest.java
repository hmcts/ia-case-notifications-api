package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

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
class AppellantRecordRefundDecisionPersonalisationSmsTest {

    private final String appellantRefundApprovedTemplateId = "appellantRefundApprovedTemplateId";
    private final String appellantRefundPartiallyApprovedTemplateId = "appellantRefundPartiallyApprovedTemplateId";
    private final String appellantRefundRejectedTemplateId = "appellantRefundRejectedTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final int daysAfterRefundDecision = 14;


    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;

    private AppellantRecordRefundDecisionPersonalisationSms appellantRecordRefundDecisionPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        String amountRemitted = "4000";
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(amountRemitted));

        appellantRecordRefundDecisionPersonalisationSms = new AppellantRecordRefundDecisionPersonalisationSms(
            appellantRefundApprovedTemplateId,
            appellantRefundPartiallyApprovedTemplateId,
            appellantRefundRejectedTemplateId,
            iaAipFrontendUrl,
            daysAfterRefundDecision,
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
                    assertEquals(appellantRefundApprovedTemplateId, appellantRecordRefundDecisionPersonalisationSms.getTemplateId(asylumCase));
            case PARTIALLY_APPROVED ->
                    assertEquals(appellantRefundPartiallyApprovedTemplateId, appellantRecordRefundDecisionPersonalisationSms.getTemplateId(asylumCase));
            case REJECTED ->
                    assertEquals(appellantRefundRejectedTemplateId, appellantRecordRefundDecisionPersonalisationSms.getTemplateId(asylumCase));
            default -> throw new IllegalArgumentException("Unexpected remission decision: " + remissionDecision);
        }
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REFUND_DECISION_DECIDED_AIP_APPELLANT_SMS",
                appellantRecordRefundDecisionPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        String appellantMobile = "07781122334";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantMobile));

        assertTrue(appellantRecordRefundDecisionPersonalisationSms.getRecipientsList(asylumCase)
            .contains(appellantMobile));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception = 
assertThrows(NullPointerException.class, 
            () -> appellantRecordRefundDecisionPersonalisationSms.getPersonalisation((AsylumCase) null))
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
                appellantRecordRefundDecisionPersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("linkToService", iaAipFrontendUrl)
            .containsEntry("14DaysAfterRefundDecision", systemDateProvider.dueDate(daysAfterRefundDecision))
            .containsEntry("refundAmount", "40.00");
    }
}
