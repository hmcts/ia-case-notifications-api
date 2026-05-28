package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Appellant24WeeksCompleteCaseReviewSmsTest {

    private final String smsTemplateIdWithLink = "template_with_link";
    private final String smsTemplateId = "template_without_link";
    private final String iaAipFrontendUrl = "http://localhost:3000";
    private final String appealReferenceNumber = "PA/50002/2019";
    private final String tribunalReceivedDate = "2024-05-27";
    private final String appealSubmissionDate = "2024-05-20";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private RecipientsFinder recipientsFinder;

    private Appellant24WeeksReviewSms appellant24WeeksReviewSms;

    @BeforeEach
    public void setup() {
        appellant24WeeksReviewSms = new Appellant24WeeksReviewSms(
                smsTemplateIdWithLink,
                smsTemplateId,
                iaAipFrontendUrl,
                recipientsFinder
        );
    }

    private void setupDefaultMocks(YesOrNo isAdmin, YesOrNo appRepresentation) {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class))
                .thenReturn(Optional.of(isAdmin));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class))
                .thenReturn(Optional.of(appRepresentation));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of(tribunalReceivedDate));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.of(appealSubmissionDate));
    }

    @Test
    public void should_return_template_id_with_link_when_not_appellant_internal_case() {
        setupDefaultMocks(YesOrNo.NO, YesOrNo.NO);
        assertEquals(smsTemplateIdWithLink, appellant24WeeksReviewSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_template_id_without_link_when_case_is_appellant_internal_case() {
        setupDefaultMocks(YesOrNo.YES, YesOrNo.YES);
        assertEquals(smsTemplateId, appellant24WeeksReviewSms.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS",
                appellant24WeeksReviewSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_recipients_from_finder() {
        String mobilePhone = "07123456789";
        when(recipientsFinder.findReppedAppellant(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mobilePhone));

        assertTrue(appellant24WeeksReviewSms.getRecipientsList(asylumCase).contains(mobilePhone));
    }

    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {
        when(recipientsFinder.findReppedAppellant(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> appellant24WeeksReviewSms.getRecipientsList(null));
        assertEquals("asylumCase must not be null", exception.getMessage());
    }

    @Test
    public void should_return_personalisation_with_all_required_fields() {
        setupDefaultMocks(YesOrNo.NO, YesOrNo.NO);
        Map<String, String> personalisation = appellant24WeeksReviewSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
                .containsKeys("appealReferenceNumber", "appealReceivedDate", "14DaysFromDateOfDirection",
                        "42DaysFromDateOfDirection", "56DaysFromDateOfDirection", "linkToService");
    }

    @Test
    public void should_return_empty_appeal_reference_when_missing() {
        setupDefaultMocks(YesOrNo.NO, YesOrNo.YES);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.empty());

        Map<String, String> personalisation = appellant24WeeksReviewSms.getPersonalisation(asylumCase);
        assertThat(personalisation).containsEntry("appealReferenceNumber", "");
    }

    @Test
    public void should_exclude_service_link_when_not_appellant_internal_case() {
        setupDefaultMocks(YesOrNo.NO, YesOrNo.YES);
        Map<String, String> personalisation = appellant24WeeksReviewSms.getPersonalisation(asylumCase);

        assertThat(personalisation).doesNotContainKey("linkToService");
    }

    @Test
    public void should_include_service_link_when_appellant_internal_case() {
        setupDefaultMocks(YesOrNo.YES, YesOrNo.NO);
        Map<String, String> personalisation = appellant24WeeksReviewSms.getPersonalisation(asylumCase);

        assertThat(personalisation).containsKey("linkToService");
    }

    @Test
    public void should_calculate_future_dates_correctly() {
        setupDefaultMocks(YesOrNo.NO, YesOrNo.YES);
        Map<String, String> personalisation = appellant24WeeksReviewSms.getPersonalisation(asylumCase);

        assertThat(personalisation)
                .containsKeys("14DaysFromDateOfDirection", "42DaysFromDateOfDirection", "56DaysFromDateOfDirection");
    }
}