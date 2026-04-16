package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionStatus.SUBMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.AWAITING_REASONS_FOR_APPEAL;

import java.time.LocalDateTime;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.TimeExtensionFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantReviewTimeExtensionRefusedPersonalisationSmsTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    TimeExtensionFinder timeExtensionFinder;

    private final String smsTemplateId = "someSmsTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";

    private final String mockedAppealReferenceNumber = "someReferenceNumber";

    private final String expectedTimeExtensionNewDate = "1 Apr 2020";

    private final String timeExtensionDecisionReason = "the reason";

    private IdValue<TimeExtension> mockedTimeExtension;

    private AppellantReviewTimeExtensionRefusedPersonalisationSms appellantReviewTimeExtensionRefusedPersonalisationSms;

    @BeforeEach
    public void setup() {

        String timeExtensionReason = "the reason";
        String timeExtensionNewDate = "2020-04-01";
        String timeExtensionRequestDate = "2020-03-01";
        mockedTimeExtension = new IdValue<>("someId", new TimeExtension(
            timeExtensionRequestDate,
            timeExtensionReason,
            AWAITING_REASONS_FOR_APPEAL,
            SUBMITTED,
            null,
            TimeExtensionDecision.REFUSED,
            timeExtensionDecisionReason,
            timeExtensionNewDate)
        );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantReviewTimeExtensionRefusedPersonalisationSms =
            new AppellantReviewTimeExtensionRefusedPersonalisationSms(
                smsTemplateId,
                iaAipFrontendUrl,
                recipientsFinder,
                timeExtensionFinder
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantReviewTimeExtensionRefusedPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_REVIEW_TIME_EXTENSION_REFUSED_APPELLANT_AIP_SMS", appellantReviewTimeExtensionRefusedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception =
assertThrows(NullPointerException.class, () -> appellantReviewTimeExtensionRefusedPersonalisationSms.getRecipientsList(null))
            ;
assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        String mockedAppellantMobilePhone = "07123456789";
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantReviewTimeExtensionRefusedPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        NullPointerException exception =
assertThrows(NullPointerException.class,
            () -> appellantReviewTimeExtensionRefusedPersonalisationSms.getPersonalisation((Callback<AsylumCase>) null))
            ;
assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        String awaitingReasonsForAppealNextActionText = "why you think the Home Office decision is wrong";

        when(callback.getCaseDetails())
            .thenReturn(new CaseDetails<>(1L, "IA", AWAITING_REASONS_FOR_APPEAL, asylumCase, LocalDateTime.now()));
        when(timeExtensionFinder
            .findCurrentTimeExtension(AWAITING_REASONS_FOR_APPEAL, TimeExtensionStatus.REFUSED, asylumCase))
            .thenReturn(mockedTimeExtension);
        when(timeExtensionFinder.findNextActionText(AWAITING_REASONS_FOR_APPEAL))
            .thenReturn(awaitingReasonsForAppealNextActionText);

        Map<String, String> personalisation =
            appellantReviewTimeExtensionRefusedPersonalisationSms.getPersonalisation(callback);
        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", mockedAppealReferenceNumber)
            .containsEntry("decision reason", timeExtensionDecisionReason)
            .containsEntry("Next action text", awaitingReasonsForAppealNextActionText)
            .containsEntry("due date", expectedTimeExtensionNewDate)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        String awaitingReasonsForAppealNextActionText = "why you think the Home Office decision is wrong";

        when(callback.getCaseDetails())
            .thenReturn(new CaseDetails<>(1L, "IA", AWAITING_REASONS_FOR_APPEAL, asylumCase, LocalDateTime.now()));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(timeExtensionFinder
            .findCurrentTimeExtension(AWAITING_REASONS_FOR_APPEAL, TimeExtensionStatus.REFUSED, asylumCase))
            .thenReturn(mockedTimeExtension);
        when(timeExtensionFinder.findNextActionText(AWAITING_REASONS_FOR_APPEAL))
            .thenReturn(awaitingReasonsForAppealNextActionText);

        Map<String, String> personalisation =
            appellantReviewTimeExtensionRefusedPersonalisationSms.getPersonalisation(callback);
        assertThat(personalisation)
            .containsEntry("Appeal Ref Number", "")
            .containsEntry("decision reason", timeExtensionDecisionReason)
            .containsEntry("Next action text", awaitingReasonsForAppealNextActionText)
            .containsEntry("due date", expectedTimeExtensionNewDate)
            .containsEntry("Hyperlink to service", iaAipFrontendUrl);
    }
}
