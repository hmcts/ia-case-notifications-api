package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AiPRemissionRequestAutomaticReminderEmailTest {

    private final String paymentRejectedReminderTemplateId = "paymentRejectedReminderTemplateId";
    private final String paymentPartiallyApprovedReminderTemplateId = "paymentPartiallyApprovedReminderTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
    private final String appellantGivenNames = "GivenNames";
    private final String appellantFamilyName = "FamilyName";
    private final String someTestDateEmail = "14/14/2024";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    private AipRemissionRequestAutomaticReminderEmail aipRemissionRequestAutomaticReminderEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(REMISSION_REJECTED_DATE_PLUS_14DAYS, String.class)).thenReturn(Optional.of(someTestDateEmail));


        aipRemissionRequestAutomaticReminderEmail = new AipRemissionRequestAutomaticReminderEmail(
            paymentRejectedReminderTemplateId,
            paymentPartiallyApprovedReminderTemplateId,
            iaAipFrontendUrl,
            recipientsFinder
        );
    }

    @Test
    void should_return_given_template_id_if_partially_approved() {
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(PARTIALLY_APPROVED));

        assertEquals(paymentPartiallyApprovedReminderTemplateId, aipRemissionRequestAutomaticReminderEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_template_id_if_rejected() {
        when(asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(REJECTED));

        assertEquals(paymentRejectedReminderTemplateId, aipRemissionRequestAutomaticReminderEmail.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_AIP_REMISSION_REMINDER_DECISION_EMAIL",
            aipRemissionRequestAutomaticReminderEmail.getReferenceId(caseId));
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
            aipRemissionRequestAutomaticReminderEmail.getPersonalisation(asylumCase);

        String amountLeftToPayInGbp = "40.00";
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("linkToOnlineService", iaAipFrontendUrl)
            .containsEntry("feeAmount", amountLeftToPayInGbp)
            .containsEntry("deadline", someTestDateEmail)
            .containsEntry("onlineCaseReferenceNumber", onlineCaseReferenceNumber)
            .containsEntry("feeAmountRejected", "140.00");
    }
}