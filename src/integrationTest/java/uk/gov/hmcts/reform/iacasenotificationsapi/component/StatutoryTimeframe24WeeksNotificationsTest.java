package uk.gov.hmcts.reform.iacasenotificationsapi.component;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.WithNotificationEmailStub;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.AsylumCaseForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.INTERNAL_APPELLANT_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.COMPLETE_CASE_REVIEW;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

@Slf4j
public class StatutoryTimeframe24WeeksNotificationsTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
        WithNotificationEmailStub {

    public static final String APPELLANT_MAIL = "appellant@domain.com";
    public static final String LR_EMAIL = "legalrep@domain.com";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL = "STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER = "STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS = "STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS";

    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL = "STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL = "STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL";
    private static final String someNotificationId = UUID.randomUUID().toString();
    private static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL";
    private static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL";
    private static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER";
    private static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL";

    private static final YesOrNo WANTS_EMAIL = YesOrNo.YES;
    private static final YesOrNo WANTS_SMS = YesOrNo.YES;
    private static final YesOrNo DONT_WANTS_SMS = YesOrNo.NO;
    private static final YesOrNo DONT_WANTS_EMAIL = YesOrNo.NO;
    @MockBean
    private GovNotifyNotificationSender notificationSender;

    // --- Test data builders / helpers ---

    private static AsylumCaseForTest mockCaseData(String lrEmail, String appellantEmail, String internalAppellantEmail, YesOrNo inCountry, YesOrNo wantsEmail, YesOrNo wantsSms) {
        return anAsylumCase()
                .with(HEARING_CENTRE, HearingCentre.MANCHESTER)
                .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                .with(EMAIL, appellantEmail)
                .with(INTERNAL_APPELLANT_EMAIL, internalAppellantEmail)
                .with(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, lrEmail)
                .with(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, APPEAL_SUBMITTED)
                .with(SUBSCRIPTIONS, anySubscriptions(wantsEmail, wantsSms))
                .with(COMPLETE_CASE_REVIEW_DATE, "2002-02-02")
                .with(APPEAL_SUBMISSION_DATE, "2002-02-02")
                .with(TRIBUNAL_RECEIVED_DATE, "2002-02-02")
                .with(HOME_OFFICE_DECISION_DATE, "2002-02-02")
                .with(APPELLANT_IN_UK, inCountry)
                .with(AsylumCaseDefinition.APPELLANT_ADDRESS, new AddressUk("l1", "l2", "l2", "pt", "county", "pc", "uk"));
    }

    private static @NotNull Optional<List<IdValue<Subscriber>>> anySubscriptions(YesOrNo wantsEmail, YesOrNo wantsSms) {
        Subscriber subscriber = new Subscriber(
                SubscriberType.APPELLANT,
                "ppellantInSubscription@gmail.com",
                wantsEmail,
                "",
                wantsSms
        );

        return Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber)));
    }

    // --- Notification assertion helpers to eliminate duplication ---
    private static Optional<List<IdValue<String>>> readNotifications(PreSubmitCallbackResponseForTest response) {
        return response.getData().read(NOTIFICATIONS_SENT);
    }

    private static void assertNoNotifications(PreSubmitCallbackResponseForTest response) {
        Optional<List<IdValue<String>>> maybe = readNotifications(response);
        assertFalse(maybe.isPresent());
    }

    private static void assertNotificationsContain(PreSubmitCallbackResponseForTest response, int expectedSize, String... expectedIds) {
        Optional<List<IdValue<String>>> maybe = readNotifications(response);
        assertTrue(maybe.isPresent());
        List<IdValue<String>> notifications = maybe.get();
        assertThat(notifications.size()).isEqualTo(expectedSize);

        String idsString = String.join(",", notifications.stream().map(IdValue::getId).toList());
        for (String id : expectedIds) {
            assertThat(idsString).contains(id);
        }
    }

    private PreSubmitCallbackResponseForTest mockResponse(AsylumCaseForTest caseData, Event event) {
        addServiceAuthStub(server);
        addNotificationEmailStub(server);

        when(notificationSender.sendEmail(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
                .thenReturn(someNotificationId);

        when(notificationSender.sendLetter(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
                .thenReturn(someNotificationId);

        when(notificationSender.sendSms(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
                .thenReturn(someNotificationId);

        return aboutToSubmit(callback()
                .event(event)
                .caseDetails(someCaseDetailsWith()
                        .state(APPEAL_SUBMITTED)
                        .caseData(caseData)));
    }

    private PreSubmitCallbackResponseForTest aboutToSubmit(CallbackForTest.CallbackForTestBuilder callback) {
        try {
            MvcResult response = mockMvc
                    .perform(
                            post("/asylum/ccdAboutToSubmit")
                                    .content(objectMapper.writeValueAsString(callback.build()))
                                    .contentType(APPLICATION_JSON_VALUE)
                    )
                    .andReturn();

            return objectMapper.readValue(
                    response.getResponse().getContentAsString(),
                    PreSubmitCallbackResponseForTest.class
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // --- Tests ---
    @Nested
    @DisplayName("REMOVE_STATUTORY_TIMEFRAME_24_WEEKS")
    class RemoveStatutoryTimeframe24WeeksNotificationsTest {

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_send_24weeks_remove_email_to_all_three_users() {
            var response = mockResponse(mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES, WANTS_EMAIL, YesOrNo.NO), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
            assertNotificationsContain(response, 3, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_send_24weeks_remove_letter_to_appellant_and_legal_rep_and_home_office_when_appellant_emails_are_empty() {
            var response = mockResponse(mockCaseData(LR_EMAIL, "", "", YesOrNo.YES, WANTS_EMAIL, YesOrNo.NO), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
            assertNotificationsContain(response, 3, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_send_24weeks_remove_email_to_appellant_and_ho_office() {
            var response = mockResponse(mockCaseData(null, APPELLANT_MAIL, null, YesOrNo.YES, WANTS_EMAIL, YesOrNo.NO), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
            assertNotificationsContain(response, 2, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_send_24weeks_remove_email_to_appellant_with_internal_email_and_ho_office() {
            var response = mockResponse(mockCaseData(null, null, APPELLANT_MAIL, YesOrNo.YES, WANTS_EMAIL, YesOrNo.NO), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
            assertNotificationsContain(response, 2, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_send_24weeks_remove_letter_to_appellant_and_email_to_ho_office() {
            var response = mockResponse(mockCaseData(null, null, null, YesOrNo.YES, WANTS_EMAIL, YesOrNo.NO), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
            assertNotificationsContain(response, 2, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_remove_letter_to_appellant_if_application_is_ooc() {
            var response = mockResponse(mockCaseData(null, null, null, YesOrNo.NO, WANTS_EMAIL, YesOrNo.NO), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
            assertNotificationsContain(response, 1, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_send_24weeks_remove_letter_to_appellant_and_email_to_legal_rep_and_home_office() {
            var response = mockResponse(mockCaseData(LR_EMAIL, null, null, YesOrNo.YES, WANTS_EMAIL, YesOrNo.NO), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
            assertNotificationsContain(response, 3, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL, REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER);
        }
    }

    @Nested
    @DisplayName("COMPLETE_REVIEW_STATUTORY_TIMEFRAME_24_WEEKS")
    class CompleteReviewStatutoryTimeframe24WeeksNotificationsTest {
        private void createdByAdmin(AsylumCaseForTest caseData) {
            caseData.with(IS_ADMIN, YesOrNo.YES);
        }

        private void notCreatedByAdmin(AsylumCaseForTest caseData) {
            caseData.with(IS_ADMIN, YesOrNo.NO);
        }


        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_case_review_email_for_bau_case() {
            var caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES, WANTS_EMAIL, YesOrNo.NO);
            createdByAdmin(caseData);
            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNoNotifications(response);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_case_review_email_for_bau_case_not_created_by_admin() {
            var caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES, WANTS_EMAIL, YesOrNo.NO);
            notCreatedByAdmin(caseData);
            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNoNotifications(response);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_case_review_email_or_letter_to_all_three_if_statutory_time_frame_is_not_present() {
            var caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES, WANTS_EMAIL, DONT_WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.NO);
            notCreatedByAdmin(caseData);
            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNoNotifications(response);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_case_review_email_or_letter_to_all_three_if_statutory_time_frame_is_not_present_created_by_admin() {
            var caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES, WANTS_EMAIL, DONT_WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.NO);
            createdByAdmin(caseData);
            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNoNotifications(response);
        }


        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_case_review_email_for_stf_24Weeks_is_no() {
            var caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES, WANTS_EMAIL, DONT_WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.NO);
            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNoNotifications(response);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_send_24weeks_case_review_email_to_all_three() {
            var caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES, WANTS_EMAIL, DONT_WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES);

            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNotificationsContain(response, 3, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_send_24weeks_review_letter_to_appellant_and_email_to_ho_office_and_lr_created_by_admin() {
            var caseData = mockCaseData(LR_EMAIL, null, null, YesOrNo.YES, DONT_WANTS_EMAIL, DONT_WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES);
            createdByAdmin(caseData);
            caseData.with(SUBSCRIPTIONS, Optional.of(new ArrayList<>()));

            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNotificationsContain(response, 3, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_LETTER, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_review_letter_to_appellant_if_case_is_not_internal() {
            var caseData = mockCaseData(LR_EMAIL, null, null, YesOrNo.YES, YesOrNo.YES, DONT_WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES);
            notCreatedByAdmin(caseData);
            caseData.with(SUBSCRIPTIONS, Optional.of(new ArrayList<>()));

            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNotificationsContain(response, 2, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL);
        }


        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_review_one_sms_and_three_emails_to_all() {
            var caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES, DONT_WANTS_EMAIL, WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES);
            createdByAdmin(caseData);
            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNotificationsContain(response, 3, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_send_24weeks_review_one_sms_and_three_emails_to_all_not_created_by_admin() {
            var caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES, DONT_WANTS_EMAIL, WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES);
            setSmsContactPreference(caseData);
            notCreatedByAdmin(caseData);
            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNotificationsContain(response, 4, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_SMS, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_review_one_sms_and_three_emails_to_all_not_created_by_admin() {
            var caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES, DONT_WANTS_EMAIL, WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.NO);
            notCreatedByAdmin(caseData);
            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNoNotifications(response);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_review_letter_to_appellant_if_has_subscription() {
            var caseData = mockCaseData(LR_EMAIL, null, null, YesOrNo.YES, WANTS_EMAIL, DONT_WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES);
            createdByAdmin(caseData);
            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNotificationsContain(response, 2, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL);
        }

        @Test
        @WithMockUser(authorities = {"caseworker-ia-system"})
        void should_not_send_24weeks_review_sms_to_appellant_if_has_subscription_and_not_created_by_admin() {
            var caseData = mockCaseData(LR_EMAIL, null, null, YesOrNo.YES, DONT_WANTS_EMAIL, WANTS_SMS);
            caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES);
            createdByAdmin(caseData);
            var response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
            assertNotificationsContain(response, 2, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL, STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL);
        }
    }

    private void setSmsContactPreference(AsylumCaseForTest caseData) {
        caseData.with(CONTACT_PREFERENCE, ContactPreference.WANTS_SMS);
        caseData.with(MOBILE_NUMBER, "07123456789");
    }

}