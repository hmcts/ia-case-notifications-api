package uk.gov.hmcts.reform.iacasenotificationsapi.component;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

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
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.INTERNAL_APPELLANT_EMAIL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.COMPLETE_CASE_REVIEW;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.APPEAL_SUBMITTED;

@Slf4j
public class StatutoryTimeframe24WeeksNotificationsTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
        WithNotificationEmailStub {

    public static final String APPELLANT_MAIL = "appellant@domain.com";
    public static final String LR_EMAIL = "legalrep@domain.com";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL = "STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL = "STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL";
    public static final String STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL = "STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL";
    private static final String someNotificationId = UUID.randomUUID().toString();
    private static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL";
    private static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL";
    private static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER";
    private static final String REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL = "REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL";

    @MockBean
    private GovNotifyNotificationSender notificationSender;

    private static AsylumCaseForTest mockCaseData(String lrEmail, String appellantEmail, String internalAppellantEmail, YesOrNo inCountry) {
        return anAsylumCase()
                .with(HEARING_CENTRE, HearingCentre.MANCHESTER)
                .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                .with(EMAIL, appellantEmail)
                .with(INTERNAL_APPELLANT_EMAIL, internalAppellantEmail)
                .with(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, lrEmail)
                .with(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, APPEAL_SUBMITTED)
                .with(SUBSCRIPTIONS, anySubscriptions())
                .with(COMPLETE_CASE_REVIEW_DATE, "2002-02-02")
                .with(APPELLANT_IN_UK, inCountry)
                .with(AsylumCaseDefinition.APPELLANT_ADDRESS, new AddressUk("l1", "l2", "l2", "pt", "county", "pc", "uk"));
    }

    private static @NotNull Optional<List<IdValue<Subscriber>>> anySubscriptions() {
        Subscriber subscriber = new Subscriber(
                SubscriberType.APPELLANT, //subscriberType
                "ppellantInSubscription@gmail.com", //email
                YesOrNo.YES, // wants email
                "", //mobileNumber
                YesOrNo.NO // wants sms
        );

        return Optional.of(Collections.singletonList(new IdValue<>("foo", subscriber)));
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_email_to_all_three_users() {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();
        assertThat(notifications.size()).isEqualTo(3);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_letter_to_appellant_and_email_to_legal_rep_and_home_office() {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(LR_EMAIL, null, null, YesOrNo.YES), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(3);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_letter_to_appellant_and_legal_rep_and_home_office_when_appellant_emails_are_empty() {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(LR_EMAIL, "", "", YesOrNo.YES), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(3);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_LEGAL_REP_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_email_to_appellant_and_ho_office() {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(null, APPELLANT_MAIL, null, YesOrNo.YES), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(2);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_email_to_appellant_with_internal_email_and_ho_office() {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(null, null, APPELLANT_MAIL, YesOrNo.YES), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);
        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(2);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_remove_letter_to_appellant_and_email_to_ho_office() {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(null, null, null, YesOrNo.YES), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(2);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_APPELLANT_LETTER);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_not_send_24weeks_remove_letter_to_appellant_if_application_is_ooc() {
        PreSubmitCallbackResponseForTest response = mockResponse(mockCaseData(null, null, null, YesOrNo.NO), REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();

        assertThat(notifications.size()).isEqualTo(1);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(REMOVE_STATUTORY_TIMEFRAME_24WEEKS_HOME_OFFICE_EMAIL);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_send_24weeks_case_review_email_to_all_three() {
        AsylumCaseForTest caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES);
        caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES);
        PreSubmitCallbackResponseForTest response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertTrue(notificationsSent.isPresent());
        List<IdValue<String>> notifications = notificationsSent.get();
        assertThat(notifications.size()).isEqualTo(3);
        List<String> idList = notifications.stream().map(IdValue::getId).toList();
        String idsString = String.join(",", idList);
        assertThat(idsString).contains(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_APPELLANT_EMAIL);
        assertThat(idsString).contains(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_LEGAL_REP_EMAIL);
        assertThat(idsString).contains(STATUTORY_TIMEFRAME_24WEEKS_CASE_REVIEW_HOME_OFFICE_EMAIL);
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-system"})
    void should_not_send_24weeks_case_review_email_to_all_three_if_statutory_time_frame_is_not_present() {
        AsylumCaseForTest caseData = mockCaseData(LR_EMAIL, APPELLANT_MAIL, null, YesOrNo.YES);
        caseData.with(AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.NO);
        PreSubmitCallbackResponseForTest response = mockResponse(caseData, COMPLETE_CASE_REVIEW);
        Optional<List<IdValue<String>>> notificationsSent =
                response
                        .getData()
                        .read(NOTIFICATIONS_SENT);

        assertFalse(notificationsSent.isPresent());
    }

    private PreSubmitCallbackResponseForTest mockResponse(AsylumCaseForTest caseData, Event event) {
        addServiceAuthStub(server);
        addNotificationEmailStub(server);
        when(notificationSender.sendEmail(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
                .thenReturn(someNotificationId);

        when(notificationSender.sendLetter(anyString(), anyString(), anyMap(), anyString(), any(Callback.class)))
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
            // test will fail
            throw new RuntimeException(e);
        }
    }

}
