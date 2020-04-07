package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionStatus.SUBMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.AWAITING_REASONS_FOR_APPEAL;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.TimeExtensionFinder;


@RunWith(MockitoJUnitRunner.class)
public class AppellantReviewTimeExtensionGrantedPersonalisationSmsTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    TimeExtensionFinder timeExtensionFinder;

    private Long caseId = 12345L;
    private String smsTemplateId = "someSmsTemplateId";
    private String iaAipFrontendUrl = "http://localhost";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";

    private String timeExtensionNewDate = "2020-04-1";
    private String timeExtensionReason = "the reason";


    private IdValue<TimeExtension> mockedTimeExtension;

    private AppellantReviewTimeExtensionGrantedPersonalisationSms appellantReviewTimeExtensionGrantedPersonalisationSms;

    @Before
    public void setup() {

        mockedTimeExtension = new IdValue<>("someId", new TimeExtension(
            timeExtensionNewDate,
            timeExtensionReason,
            AWAITING_REASONS_FOR_APPEAL,
            SUBMITTED,
            null,
            TimeExtensionDecision.GRANTED,
            null,
            timeExtensionNewDate)
        );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantReviewTimeExtensionGrantedPersonalisationSms = new AppellantReviewTimeExtensionGrantedPersonalisationSms(
            smsTemplateId,
            iaAipFrontendUrl,
            recipientsFinder,
            timeExtensionFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        Assert.assertEquals(smsTemplateId, appellantReviewTimeExtensionGrantedPersonalisationSms.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        Assert.assertEquals(caseId + "_REVIEW_TIME_EXTENSION_GRANTED_APPELLANT_AIP_SMS", appellantReviewTimeExtensionGrantedPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantReviewTimeExtensionGrantedPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS)).thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantReviewTimeExtensionGrantedPersonalisationSms.getRecipientsList(asylumCase).contains(mockedAppellantMobilePhone));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> appellantReviewTimeExtensionGrantedPersonalisationSms.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        String awaitingReasonsForAppealNextActionText = "why you think the Home Office decision is wrong";

        when(callback.getCaseDetails()).thenReturn(new CaseDetails<>(1L, "IA", AWAITING_REASONS_FOR_APPEAL, asylumCase, LocalDateTime.now()));
        when(timeExtensionFinder.findCurrentTimeExtension(AWAITING_REASONS_FOR_APPEAL, asylumCase)).thenReturn(mockedTimeExtension);
        when(timeExtensionFinder.findNextActionText(AWAITING_REASONS_FOR_APPEAL)).thenReturn(awaitingReasonsForAppealNextActionText);

        Map<String, String> personalisation = appellantReviewTimeExtensionGrantedPersonalisationSms.getPersonalisation(callback);
        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(awaitingReasonsForAppealNextActionText, personalisation.get("Next action text"));
        assertEquals(timeExtensionNewDate, personalisation.get("due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

    }

    @Test
    public void should_return_personalisation_when_only_mandatory_information_given() {

        String awaitingReasonsForAppealNextActionText = "why you think the Home Office decision is wrong";

        when(callback.getCaseDetails()).thenReturn(new CaseDetails<>(1L, "IA", AWAITING_REASONS_FOR_APPEAL, asylumCase, LocalDateTime.now()));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(timeExtensionFinder.findCurrentTimeExtension(AWAITING_REASONS_FOR_APPEAL, asylumCase)).thenReturn(mockedTimeExtension);
        when(timeExtensionFinder.findNextActionText(AWAITING_REASONS_FOR_APPEAL)).thenReturn(awaitingReasonsForAppealNextActionText);

        Map<String, String> personalisation = appellantReviewTimeExtensionGrantedPersonalisationSms.getPersonalisation(callback);
        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(awaitingReasonsForAppealNextActionText, personalisation.get("Next action text"));
        assertEquals(timeExtensionNewDate, personalisation.get("due date"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
    }
}
