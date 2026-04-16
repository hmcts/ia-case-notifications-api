package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.REJECTED;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepRemissionPaymentReminderPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    private final String paymentRejectedReminderTemplateId = "paymentRejectedReminderTemplateId";
    private final String paymentPartiallyApprovedReminderTemplateId = "paymentPartiallyApprovedReminderTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String appellantGivenNames = "GivenNames";
    private final String appellantFamilyName = "FamilyName";
    private final String legalRepRefNumber = "somelegalRepRefNumber";
    private final String someTestDateEmail = "14/14/2024";
    private LegalRepRemissionPaymentReminderPersonalisation legalRepRemissionPaymentReminderPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(REMISSION_REJECTED_DATE_PLUS_14DAYS, String.class)).thenReturn(Optional.of(someTestDateEmail));


        legalRepRemissionPaymentReminderPersonalisation = new LegalRepRemissionPaymentReminderPersonalisation(
            paymentRejectedReminderTemplateId,
            paymentPartiallyApprovedReminderTemplateId,
            iaExUiFrontendUrl

        );
    }

    @Test
    void should_return_given_template_id_if_partially_approved() {
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(PARTIALLY_APPROVED));

        assertEquals(paymentPartiallyApprovedReminderTemplateId, legalRepRemissionPaymentReminderPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_template_id_if_rejected() {
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(REJECTED));

        assertEquals(paymentRejectedReminderTemplateId, legalRepRemissionPaymentReminderPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REMISSION_REMINDER_DECISION_LEGAL_REPRESENTATIVE",
            legalRepRemissionPaymentReminderPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        String onlineCaseReferenceNumber = "1111222233334444";
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        String amountLeftToPay = "4000";
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        String feeAmount = "14000";
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));

        Map<String, String> personalisation =
            legalRepRemissionPaymentReminderPersonalisation.getPersonalisation(asylumCase);

        String amountLeftToPayInGbp = "40.00";
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("legalRepReferenceNumber", legalRepRefNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaExUiFrontendUrl)
            .containsEntry("feeAmount", amountLeftToPayInGbp)
            .containsEntry("deadline", someTestDateEmail)
            .containsEntry("onlineCaseReferenceNumber", onlineCaseReferenceNumber)
            .containsEntry("feeAmountRejected", "140.00");
    }
}


