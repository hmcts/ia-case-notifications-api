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
class PaPayLaterListingPersonalisationSmsTest {

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private RecipientsFinder recipientsFinder;

    @Mock
    private SystemDateProvider systemDateProvider;

    private PaPayLaterListingPersonalisationSms paPayLaterListingPersonalisationSms;

    private final Long caseId = 12345L;
    private final String templateId = "PaPayLaterListingTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final int daysAfterNotificationSent = 14;
    private String appealReferenceNumber = "appealReferenceNumber";
    private String withHearing = "decisionWithHearing";
    private String withoutHearing = "decisionWithoutHearing";
    private int daysAfterRemissionDecision = 14;
    private String newFeeAmount = "8000";

    @BeforeEach
    void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of(newFeeAmount));
        when(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withHearing));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(withoutHearing));

        paPayLaterListingPersonalisationSms = new PaPayLaterListingPersonalisationSms(
                templateId,
                daysAfterNotificationSent,
                iaAipFrontendUrl,
                recipientsFinder,
                systemDateProvider
        );
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(
                caseId + "_PA_PAY_LATER_LISTING_SMS",
                paPayLaterListingPersonalisationSms.getReferenceId(caseId)
        );
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
                paPayLaterListingPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals("Decision with hearing", personalisation.get("previousDecisionHearingFeeOption"));
        assertEquals("Decision without hearing", personalisation.get("updatedDecisionHearingFeeOption"));
        assertEquals("80.00", personalisation.get("newFee"));
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("dueDate"));
    }
}
