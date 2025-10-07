package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
class AppellantSubmittedWithRemissionRequestPersonalisationSmsTest {

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private RecipientsFinder recipientsFinder;

    @Mock
    private SystemDateProvider systemDateProvider;

    private final String smsTemplateId = "sms-template-id";
    private final String paPayLaterSmsTemplateId = "pa-pay-later-sms-template-id";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String mockedMobile = "07123456789";
    private final Long caseId = 12345L;

    private AppellantSubmittedWithRemissionRequestPersonalisationSms personalisation;

    @BeforeEach
    void setUp() {
        personalisation = new AppellantSubmittedWithRemissionRequestPersonalisationSms(
                smsTemplateId,
                paPayLaterSmsTemplateId,
                14,
                iaAipFrontendUrl,
                14,
                recipientsFinder,
                systemDateProvider
        );
    }

    @Test
    void should_return_base_template_id_when_not_pa_or_no_payment_option() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));

        String result = personalisation.getTemplateId(asylumCase);
        assertEquals(smsTemplateId, result);
    }

    @Test
    void should_return_pa_pay_later_template_id_when_payment_option_is_payLater() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        String result = personalisation.getTemplateId(asylumCase);
        assertEquals(paPayLaterSmsTemplateId, result);
    }

    @Test
    void should_return_pa_pay_later_template_id_when_payment_option_is_payOffline() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        String result = personalisation.getTemplateId(asylumCase);
        assertEquals(paPayLaterSmsTemplateId, result);
    }

    @Test
    void should_return_base_template_id_when_pa_and_payment_option_is_payNow() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        String result = personalisation.getTemplateId(asylumCase);
        assertEquals(smsTemplateId, result);
    }

    @Test
    void should_return_reference_id() {
        String result = personalisation.getReferenceId(caseId);
        assertEquals(caseId + "_SUBMITTED_WITH_REMISSION_REQUEST_AIP_SMS", result);
    }

    @Test
    void should_return_recipients_list() {
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS)).thenReturn(Set.of(mockedMobile));

        Set<String> result = personalisation.getRecipientsList(asylumCase);
        assertTrue(result.contains(mockedMobile));
    }

    @Test
    void should_return_personalisation_for_pa_pay_later() {
        String dueDate = "20 Oct 2025";
        when(systemDateProvider.dueDate(14)).thenReturn(dueDate);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("REF123"));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("HO123"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertEquals("REF123", result.get("appealReferenceNumber"));
        assertEquals("HO123", result.get("homeOfficeReferenceNumber"));
        assertEquals("John", result.get("appellantGivenNames"));
        assertEquals("Doe", result.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, result.get("Hyperlink to service"));
        assertEquals(dueDate, result.get("14 days after remission request sent"));
    }

    @Test
    void should_return_personalisation_for_non_pa() {
        String dueDate = "21 Oct 2025";
        when(systemDateProvider.dueDate(14)).thenReturn(dueDate);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("REF999"));

        Map<String, String> result = personalisation.getPersonalisation(asylumCase);

        assertEquals("REF999", result.get("Appeal Ref Number"));
        assertEquals(dueDate, result.get("appealSubmittedDaysAfter"));
        assertEquals(iaAipFrontendUrl, result.get("Hyperlink to service"));
    }

    @Test
    void should_throw_exception_when_asylum_case_is_null() {
        assertThatThrownBy(() -> personalisation.getPersonalisation(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }
}
