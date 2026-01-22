package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NEW_FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PREVIOUS_DECISION_HEARING_FEE_OPTION;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaPayLaterDecisionPersonalisationSmsTest {

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private RecipientsFinder recipientsFinder;

    @Mock
    private SystemDateProvider systemDateProvider;

    private PaPayLaterDecisionPersonalisationSms personalisation;

    private final Long caseId = 12345L;
    private final String templateId = "PaPayLaterDecisionTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final int daysAfterNotificationSent = 14;
    private final String appealReferenceNumber = "appealReferenceNumber";
    private final String withHearing = "decisionWithHearing";
    private final String withoutHearing = "decisionWithoutHearing";
    private final String newFeeAmount = "8000";

    @BeforeEach
    void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of(newFeeAmount));
        when(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withHearing));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withoutHearing));

        personalisation = new PaPayLaterDecisionPersonalisationSms(
                templateId,
                daysAfterNotificationSent,
                iaAipFrontendUrl,
                recipientsFinder,
                systemDateProvider
        );
    }

    @Test
    void should_return_reference_id() {
        assertEquals(caseId + "_PA_PAY_LATER_DECISION_SMS", personalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        final String dueDate = "14 Apr 2026";
        when(systemDateProvider.dueDate(daysAfterNotificationSent)).thenReturn(dueDate);

        Map<String, String> map = personalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, map.get("appealReferenceNumber"));
        assertEquals("Decision with hearing", map.get("previousDecisionHearingFeeOption"));
        assertEquals("Decision without hearing", map.get("updatedDecisionHearingFeeOption"));
        assertEquals("80.00", map.get("newFee"));
        assertEquals(dueDate, map.get("dueDate"));
    }
}
