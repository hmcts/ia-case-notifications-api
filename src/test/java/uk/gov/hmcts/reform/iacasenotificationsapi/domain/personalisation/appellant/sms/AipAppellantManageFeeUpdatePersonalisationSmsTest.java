package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MANAGE_FEE_REQUESTED_AMOUNT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PREVIOUS_FEE_AMOUNT_GBP;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipAppellantManageFeeUpdatePersonalisationSmsTest {
    private final String aipAppellantManageFeeUpdateSmsTemplateId = "aipAppellantManageFeeUpdateSmsTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String mockedAppellantMobilePhone = "07123456789";
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String onlineCaseReferenceNumber = "1111222233334444";
    private final int daysAfterRemissionDecision = 14;

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    FeatureToggler featureToggler;

    private AipAppellantManageFeeUpdatePersonalisationSms aipAppellantManageFeeUpdatePersonalisationSms;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        String feeAmount = "4000";
        when(asylumCase.read(PREVIOUS_FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));
        String newFeeAmount = "2000";
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(newFeeAmount));
        String manageFeeRequestedAmount = "2000";
        when(asylumCase.read(MANAGE_FEE_REQUESTED_AMOUNT, String.class)).thenReturn(Optional.of(manageFeeRequestedAmount));

        aipAppellantManageFeeUpdatePersonalisationSms = new AipAppellantManageFeeUpdatePersonalisationSms(
            aipAppellantManageFeeUpdateSmsTemplateId,
            iaAipFrontendUrl,
            daysAfterRemissionDecision,
            recipientsFinder,
            systemDateProvider,
            featureToggler
        );
    }

    @Test
    void should_return_approved_template_id() {
        assertTrue(aipAppellantManageFeeUpdatePersonalisationSms.getTemplateId(asylumCase).contains(aipAppellantManageFeeUpdateSmsTemplateId));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_MANAGE_FEE_UPDATE_AIP_APPELLANT_SMS",
            aipAppellantManageFeeUpdatePersonalisationSms.getReferenceId(12345L));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));
        when(featureToggler.getValue("dlrm-telephony-feature-flag", false)).thenReturn(true);

        assertTrue(aipAppellantManageFeeUpdatePersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_not_return_appellant_email_address_from_asylum_case_when_flag_is_disabled() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertFalse(aipAppellantManageFeeUpdatePersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        NullPointerException exception = 
assertThrows(NullPointerException.class, 
            () -> aipAppellantManageFeeUpdatePersonalisationSms.getPersonalisation((AsylumCase) null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
            aipAppellantManageFeeUpdatePersonalisationSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("onlineCaseReferenceNumber", onlineCaseReferenceNumber)
            .containsEntry("linkToService", iaAipFrontendUrl)
            .containsEntry("dueDate", systemDateProvider.dueDate(daysAfterRemissionDecision))
            .containsEntry("originalTotalFee", "40.00")
            .containsEntry("newTotalFee", "20.00")
            .containsEntry("paymentAmount", "20.00");
    }
}
