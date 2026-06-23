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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
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
class AipAppellantRecordRemissionDecisionPaPersonalisationSmsTest {

    private final Long caseId = 12345L;
    private final String aipAppellantRemissionApprovedTemplateId = "aipAppellantRemissionApprovedTemplateId";
    private final String aipAppellantRemissionPartiallyApprovedTemplateId = "aipAppellantRemissionPartiallyApprovedTemplateId";
    private final String aipAppellantRemissionRejectedTemplateId = "aipAppellantRemissionRejectedTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String onlineCaseReferenceNumber = "1111222233334444";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer@example.com";
    private final int daysAfterRemissionDecision = 14;

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    FeatureToggler featureToggler;

    private AipAppellantRecordRemissionDecisionPaPersonalisationSms aipAppellantRecordRemissionDecisionPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        String amountLeftToPay = "4000";
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));

        aipAppellantRecordRemissionDecisionPersonalisationSms = new AipAppellantRecordRemissionDecisionPaPersonalisationSms(
            aipAppellantRemissionApprovedTemplateId,
            aipAppellantRemissionPartiallyApprovedTemplateId,
            aipAppellantRemissionRejectedTemplateId,
            iaAipFrontendUrl,
            daysAfterRemissionDecision,
            recipientsFinder,
            systemDateProvider,
            featureToggler
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
                assertEquals(aipAppellantRemissionApprovedTemplateId, aipAppellantRecordRemissionDecisionPersonalisationSms.getTemplateId(asylumCase));
            case PARTIALLY_APPROVED ->
                assertEquals(aipAppellantRemissionPartiallyApprovedTemplateId, aipAppellantRecordRemissionDecisionPersonalisationSms.getTemplateId(asylumCase));
            case REJECTED ->
                assertEquals(aipAppellantRemissionRejectedTemplateId, aipAppellantRecordRemissionDecisionPersonalisationSms.getTemplateId(asylumCase));
            default -> throw new IllegalArgumentException("Unexpected remission decision: " + remissionDecision);
        }
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_REMISSION_DECISION_DECIDED_AIP_APPELLANT_SMS",
            aipAppellantRecordRemissionDecisionPersonalisationSms.getReferenceId(12345L));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        String appellantMobile = "07781122334";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(appellantMobile));
        when(featureToggler.getValue("dlrm-telephony-feature-flag", false)).thenReturn(true);
        assertTrue(aipAppellantRecordRemissionDecisionPersonalisationSms.getRecipientsList(asylumCase)
            .contains(appellantMobile));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception =
            assertThrows(NullPointerException.class,
                () -> aipAppellantRecordRemissionDecisionPersonalisationSms.getPersonalisation((AsylumCase) null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));

        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
            aipAppellantRecordRemissionDecisionPersonalisationSms.getPersonalisation(asylumCase);

        String amountLeftToPayInGbp = "40.00";
        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("onlineCaseReferenceNumber", onlineCaseReferenceNumber)
            .containsEntry("linkToService", iaAipFrontendUrl)
            .containsEntry("payByDeadline", systemDateProvider.dueDate(daysAfterRemissionDecision))
            .containsEntry("remainingFee", amountLeftToPayInGbp);
    }
}
