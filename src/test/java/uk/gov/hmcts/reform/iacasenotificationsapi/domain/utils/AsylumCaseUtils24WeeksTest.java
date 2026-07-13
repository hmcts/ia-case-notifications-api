package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.INTERNAL_APPELLANT_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MOBILE_NUMBER;
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
    void letter_is_preferred_when_no_email_and_no_applicant_email_and_no_sms() {
        AsylumCase asylumCase = new AsylumCase();
        writeSubscribers(asylumCase, subscriber(SubscriberType.APPELLANT, "", YesOrNo.NO, "07700000000", YesOrNo.NO));

        Assertions.assertFalse(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertTrue(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void sms_is_preferred_when_subscriber_wants_sms_and_no_applicant_email() {
        AsylumCase asylumCase = new AsylumCase();
        writeSubscribers(asylumCase, subscriber(SubscriberType.APPELLANT, "", YesOrNo.NO, "07700000000", YesOrNo.YES));

        Assertions.assertTrue(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertFalse(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void sms_preference_present_but_applicant_email_also_present() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(EMAIL, "appellant@example.com");
        writeSubscribers(asylumCase, subscriber(SubscriberType.APPELLANT, "", YesOrNo.NO, "07700000000", YesOrNo.YES));

        Assertions.assertTrue(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertFalse(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void both_email_and_sms_preferences_present_for_subscriber() {
        AsylumCase asylumCase = new AsylumCase();
        writeSubscribers(asylumCase, subscriber(SubscriberType.APPELLANT, "appellant@example.com", YesOrNo.YES, "07700000000", YesOrNo.YES));

        Assertions.assertTrue(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertFalse(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void only_email_is_preferred_when_applicant_has_email_and_subscriber_wants_email_only() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(EMAIL, "appellant@example.com");
        writeSubscribers(asylumCase, subscriber(SubscriberType.APPELLANT, "appellant@example.com", YesOrNo.YES, "", YesOrNo.NO));

        Assertions.assertFalse(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertFalse(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void no_subscriptions_but_applicant_has_email_means_email_preferred() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(EMAIL, "appellant@example.com");

        Assertions.assertFalse(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertFalse(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void empty_subscriptions_list_results_in_letter_preferred() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(SUBSCRIPTIONS, Optional.of(new ArrayList<>()));

        Assertions.assertFalse(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertTrue(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void sms_not_wanted_results_in_letter_preferred() {
        AsylumCase asylumCase = new AsylumCase();
        writeSubscribers(asylumCase, subscriber(SubscriberType.APPELLANT, "", YesOrNo.NO, "07700000000", YesOrNo.NO));

        Assertions.assertFalse(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertTrue(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void sms_wanted_but_mobile_missing_still_considered_sms_preferred() {
        AsylumCase asylumCase = new AsylumCase();
        writeSubscribers(asylumCase, subscriber(SubscriberType.APPELLANT, "", YesOrNo.NO, "", YesOrNo.YES));

        Assertions.assertTrue(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertFalse(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void internal_appellant_email_counts_as_email_preference_with_sms_subscription_present() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(INTERNAL_APPELLANT_EMAIL, "internal@example.com");
        writeSubscribers(asylumCase, subscriber(SubscriberType.APPELLANT, "", YesOrNo.NO, "07700000000", YesOrNo.YES));

        Assertions.assertTrue(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertFalse(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void multiple_subscribers_where_one_prefers_email_and_one_prefers_sms_results_in_both_flags() {
        AsylumCase asylumCase = new AsylumCase();
        Subscriber s1 = subscriber(SubscriberType.APPELLANT, "", YesOrNo.NO, "07700000000", YesOrNo.YES);
        Subscriber s2 = subscriber(SubscriberType.APPELLANT, "appellant@example.com", YesOrNo.YES, "", YesOrNo.NO);
        writeSubscribers(asylumCase, s1, s2);

        Assertions.assertTrue(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertFalse(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
    }

    @Test
    void at_least_one_subscriber_wants_sms_even_if_other_has_email_results_in_sms_preferred() {
        AsylumCase asylumCase = new AsylumCase();
        Subscriber s1 = subscriber(SubscriberType.APPELLANT, "", YesOrNo.NO, "07700000000", YesOrNo.YES);
        Subscriber s2 = subscriber(SubscriberType.APPELLANT, "appellant@example.com", YesOrNo.NO, "07700000000", YesOrNo.NO);
        writeSubscribers(asylumCase, s1, s2);

        Assertions.assertTrue(AsylumCaseUtils.isSmsPreferred(asylumCase));
        Assertions.assertFalse(AsylumCaseUtils.isLetterOnlyPreferredCommunication(asylumCase));
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
    @DisplayName("CONTACT_PREFERENCE")
    class ContactPreferenceMobileTests {
        AsylumCase asylumCase = mock(AsylumCase.class);

        @Test
        void should_return_true_when_sms_contact_preference_is_set_and_mobile_number_exists() {
            when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
                    .thenReturn(Optional.of(ContactPreference.WANTS_SMS));
            when(asylumCase.read(MOBILE_NUMBER, String.class))
                    .thenReturn(Optional.of("07700900123"));

            boolean result = AsylumCaseUtils.hasSmsContactPreference(asylumCase);

            assertTrue(result);
        }

        @Test
        void should_return_false_when_sms_contact_preference_is_not_set() {
            when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
                    .thenReturn(Optional.empty());
            when(asylumCase.read(MOBILE_NUMBER, String.class))
                    .thenReturn(Optional.of("07700900123"));

            boolean result = AsylumCaseUtils.hasSmsContactPreference(asylumCase);

            assertFalse(result);
        }

        @Test
        void should_return_false_when_contact_preference_is_not_sms() {
            when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
                    .thenReturn(Optional.of(ContactPreference.WANTS_EMAIL));
            when(asylumCase.read(MOBILE_NUMBER, String.class))
                    .thenReturn(Optional.of("07700900123"));

            boolean result = AsylumCaseUtils.hasSmsContactPreference(asylumCase);

            assertFalse(result);
        }

        @Test
        void should_return_false_when_mobile_number_is_not_present() {
            when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
                    .thenReturn(Optional.of(ContactPreference.WANTS_SMS));
            when(asylumCase.read(MOBILE_NUMBER, String.class))
                    .thenReturn(Optional.empty());

            boolean result = AsylumCaseUtils.hasSmsContactPreference(asylumCase);

            assertFalse(result);
        }


        @Test
        void should_return_false_when_both_contact_preference_and_mobile_number_are_missing() {
            when(asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class))
                    .thenReturn(Optional.empty());
            when(asylumCase.read(MOBILE_NUMBER, String.class))
                    .thenReturn(Optional.empty());

            boolean result = AsylumCaseUtils.hasSmsContactPreference(asylumCase);

            assertFalse(result);
        }

        @Test
        void should_return_true_when_no_contact_preference_but_has_subscriptions() {

            AsylumCase asylumCase = new AsylumCase();
            writeSubscribers(asylumCase, subscriber(SubscriberType.APPELLANT, "", YesOrNo.NO, "07700000000", YesOrNo.YES));

            boolean result = AsylumCaseUtils.hasSmsContactPreference(asylumCase);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("isCaseReviewFor24WeeksCase")
    class IsCaseReviewFor24WeeksCaseTests {

        // Scenario 1 - BAU in-country case with 24-week flag set to Yes → should fire
        @Test
        void should_return_true_for_bau_in_country_case_with_24_week_flag() {
            AsylumCase asylumCase = mock(AsylumCase.class);
            when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));
            when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YES));

            assertTrue(AsylumCaseUtils.isCaseReviewFor24WeeksCase(COMPLETE_CASE_REVIEW, asylumCase));
        }

        // Scenario 2 - BAU case with 24-week flag set to No → should NOT fire
        @Test
        void should_return_false_for_bau_case_when_24_week_flag_is_no() {
            AsylumCase asylumCase = mock(AsylumCase.class);
            when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));
            when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(NO));

            assertFalse(AsylumCaseUtils.isCaseReviewFor24WeeksCase(COMPLETE_CASE_REVIEW, asylumCase));
        }

        // Scenario 3 - Detained case with 24-week flag set to No → should NOT fire
        @Test
        void should_return_false_for_detained_case_when_24_week_flag_is_no() {
            AsylumCase asylumCase = mock(AsylumCase.class);
            when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));
            when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(NO));

            assertFalse(AsylumCaseUtils.isCaseReviewFor24WeeksCase(COMPLETE_CASE_REVIEW, asylumCase));
        }

        @Test
        void should_return_false_when_event_is_not_complete_case_review() {
            AsylumCase asylumCase = mock(AsylumCase.class);
            when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));
            when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YES));

            assertFalse(AsylumCaseUtils.isCaseReviewFor24WeeksCase(SUBMIT_APPEAL, asylumCase));
        }

        @Test
        void should_return_false_when_appellant_is_not_in_uk() {
            AsylumCase asylumCase = mock(AsylumCase.class);
            when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(NO));
            when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YES));

            assertFalse(AsylumCaseUtils.isCaseReviewFor24WeeksCase(COMPLETE_CASE_REVIEW, asylumCase));
        }
    }
}