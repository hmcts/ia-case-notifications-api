package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaPayLaterCaseBuildingPersonalisationSmsTest {

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private RecipientsFinder recipientsFinder;

    @Mock
    private SystemDateProvider systemDateProvider;

    private PaPayLaterCaseBuildingPersonalisationSms personalisation;

    private final Long caseId = 12345L;
    private final String templateId = "PaPayLaterCaseBuildingTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final int daysAfterNotificationSent = 14;
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String feeAmount = "14000";

    @BeforeEach
    void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));

        personalisation = new PaPayLaterCaseBuildingPersonalisationSms(
                templateId,
                iaAipFrontendUrl,
                recipientsFinder
        );
    }

    @Test
    void should_return_reference_id() {
        assertEquals(caseId + "_PA_PAY_LATER_CASE_BUILDING_SMS", personalisation.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
                () -> personalisation.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = "14 Apr 2026";
        when(systemDateProvider.dueDate(daysAfterNotificationSent)).thenReturn(dueDate);

        Map<String, String> map = personalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, map.get("appealReferenceNumber"));
        assertEquals("140.00", map.get("feeAmount"));
    }
}
