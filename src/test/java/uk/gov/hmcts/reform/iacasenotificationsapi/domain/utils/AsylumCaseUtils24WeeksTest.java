package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.COMPLETE_CASE_REVIEW;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtils24WeeksTest {

    private static Subscriber subscriber(SubscriberType type, String email, YesOrNo wantsEmail, String mobile, YesOrNo wantsSms) {
        return new Subscriber(type, email, wantsEmail, mobile, wantsSms);
    }

    private static void writeSubscribers(AsylumCase asylumCase, Subscriber... subscribers) {
        List<IdValue<Subscriber>> list = new ArrayList<>();
        for (int i = 0; i < subscribers.length; i++) {
            list.add(new IdValue<>(String.valueOf(i + 1), subscribers[i]));
        }
        asylumCase.write(SUBSCRIPTIONS, Optional.of(list));
    }

    @Test
    void has_stf_24_weeks() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
                .thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.hasStf24WeeksStatus(asylumCase));
    }

    @Test
    void stf_24_weeks_false() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class))
                .thenReturn(Optional.of(NO));
        assertFalse(AsylumCaseUtils.hasStf24WeeksStatus(asylumCase));
    }

    @Test
    void stf_24_weeks_false_if_no_yes_or_no() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        assertFalse(AsylumCaseUtils.hasStf24WeeksStatus(asylumCase));
    }


    @Test
    void should_return_tribunal_received_date_when_present() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2024-05-27"));


        String result = AsylumCaseUtils.getAppealReceivedDate(asylumCase);

        assertEquals("27 May 2024", result);
    }

    @Test
    void should_return_appeal_submission_date_when_tribunal_received_date_absent() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.of("2024-05-20"));

        String result = AsylumCaseUtils.getAppealReceivedDate(asylumCase);

        assertEquals("20 May 2024", result);
    }

    @Test
    void should_return_appeal_submission_date_when_tribunal_received_date_is_empty_string() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of(""));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.of("2024-05-20"));

        String result = AsylumCaseUtils.getAppealReceivedDate(asylumCase);

        assertEquals("20 May 2024", result);
    }

    @Test
    void should_throw_exception_when_both_tribunal_and_submission_dates_absent() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> AsylumCaseUtils.getAppealReceivedDate(asylumCase));

        assertEquals("Received date  is not present", exception.getMessage());
    }

    @Test
    void should_throw_exception_when_both_dates_are_empty_strings() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of(""));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.of(""));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> AsylumCaseUtils.getAppealReceivedDate(asylumCase));

        assertEquals("Received date  is not present", exception.getMessage());
    }

    @Test
    void should_format_date_as_d_mmm_yyyy() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2024-01-01"));


        String result = AsylumCaseUtils.getAppealReceivedDate(asylumCase);

        assertEquals("1 Jan 2024", result);
    }

    @Test
    void should_prioritize_tribunal_received_date_over_appeal_submission_date() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2024-06-15"));


        String result = AsylumCaseUtils.getAppealReceivedDate(asylumCase);

        assertEquals("15 Jun 2024", result);
    }

    @Test
    void should_handle_dates_in_december() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2024-12-31"));


        String result = AsylumCaseUtils.getAppealReceivedDate(asylumCase);

        assertEquals("31 Dec 2024", result);
    }

    @Test
    void should_handle_dates_with_single_digit_days() {
        AsylumCase asylumCase = mock(AsylumCase.class);
        when(asylumCase.read(TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2024-03-05"));


        String result = AsylumCaseUtils.getAppealReceivedDate(asylumCase);

        assertEquals("5 Mar 2024", result);
    }

    @Nested
    @DisplayName("isCaseReviewFor24WeeksCase")
    class IsCaseReviewFor24WeeksCaseTests {

        // Scenario 1 - 24-week flag set to Yes → should fire
        @Test
        void should_return_true_for_24_week_case() {
            AsylumCase asylumCase = mock(AsylumCase.class);
            when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YES));

            assertTrue(AsylumCaseUtils.isCaseReviewFor24WeeksCase(COMPLETE_CASE_REVIEW, asylumCase));
        }

        // Scenario 2 - 24-week flag set to No → should NOT fire
        @Test
        void should_return_false_when_24_week_flag_is_no() {
            AsylumCase asylumCase = mock(AsylumCase.class);
            when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(NO));

            assertFalse(AsylumCaseUtils.isCaseReviewFor24WeeksCase(COMPLETE_CASE_REVIEW, asylumCase));
        }

        // Scenario 3 - 24-week flag absent → should NOT fire
        @Test
        void should_return_false_when_24_week_flag_is_absent() {
            AsylumCase asylumCase = mock(AsylumCase.class);
            when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.empty());

            assertFalse(AsylumCaseUtils.isCaseReviewFor24WeeksCase(COMPLETE_CASE_REVIEW, asylumCase));
        }

        @Test
        void should_return_false_when_event_is_not_complete_case_review() {
            AsylumCase asylumCase = mock(AsylumCase.class);
            when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YES));

            assertFalse(AsylumCaseUtils.isCaseReviewFor24WeeksCase(SUBMIT_APPEAL, asylumCase));
        }
    }
}